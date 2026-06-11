package com.invision.web.Invision.service;

import com.invision.web.Invision.config.CustomUserDetails;
import com.invision.web.Invision.dto.LoanActionDTO;
import com.invision.web.Invision.dto.LoanRequestDTO;
import com.invision.web.Invision.dto.LoanResponseDTO;
import com.invision.web.Invision.enums.*;
import com.invision.web.Invision.exception.loan.BadLoanRequest;
import com.invision.web.Invision.exception.loan.ExceededLoanRequestException;
import com.invision.web.Invision.exception.loan.InvalidLoanStatusChangeException;
import com.invision.web.Invision.exception.loan.NoLoansFoundException;
import com.invision.web.Invision.exception.user.UserNotFoundException;
import com.invision.web.Invision.mapper.LoanMapper;
import com.invision.web.Invision.model.Asset;
import com.invision.web.Invision.model.Loan;
import com.invision.web.Invision.model.User;
import com.invision.web.Invision.repository.AssetRepository;
import com.invision.web.Invision.repository.LoanRepository;
import com.invision.web.Invision.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanService {
    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;
    private final AssetRepository assetRepository;
    private final AuditLogService auditLogService; // Inject custom helper
    private final UserRepository userRepository;
    private NotificationService notificationService;

    public List<LoanResponseDTO> getAllOverdueLoans(){
        return loanRepository.findByDueDateBeforeAndStatusNot(LocalDateTime.now(), LoanStatus.RETURNED)
                .stream()
                .map(loanMapper::loanToLoanResponseDTO)
                .toList();
    }

    public List<LoanResponseDTO> getOverdueLoansByDepartment(Department department) {
        List<Loan> loans = loanRepository.findByDueDateBeforeAndStatusNotAndUserDepartment(
                LocalDateTime.now(), LoanStatus.RETURNED, department);

        if (loans.isEmpty()) {
            throw new NoLoansFoundException("No overdue loans found for department: " + department);
        }

        return loans.stream()
                .map(loanMapper::loanToLoanResponseDTO)
                .toList();
    }

    public List<LoanResponseDTO> getUserOverdueLoans( Long userId){
        if (userRepository.existsById(userId)) {
            throw new UserNotFoundException("This user doesn't exist");
        }

        List<Loan> loans = loanRepository.findByDueDateBeforeAndStatusNotAndUserUserId(LocalDateTime.now(),LoanStatus.RETURNED,userId);
        if(loans.isEmpty()){
            throw new NoLoansFoundException("This user has no overdue loans");
        }

        return loans.stream().map(loanMapper::loanToLoanResponseDTO).toList();
    }


    public List<LoanResponseDTO> getLoansByAsset(Long assetId){
       List<Loan> loans =  loanRepository.findByAssetAssetId(assetId);

        if(loans.isEmpty()){
            throw new NoLoansFoundException("This asset has no loan history");
        }

        return loans.stream().map(loanMapper::loanToLoanResponseDTO).toList();
    }

    public List<LoanResponseDTO> getUserLoans(Long userId){
        List<Loan> loans = loanRepository.findByUserUserId(userId);

        if(loans.isEmpty()){
            throw new NoLoansFoundException("This user has no loan history");
        }

        return loans.stream().map(loanMapper::loanToLoanResponseDTO).toList();
    }

    public LoanResponseDTO updateLoanStatus(Long loanId, LoanActionDTO actionDTO){
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Loan not found"));

        LoanStatus oldStatus = loan.getStatus();
        loan.setStatus(actionDTO.loanStatus());


        Asset asset = loan.getAsset();
        String assetInfo = "Asset ID: " + (asset != null ? asset.getAssetId() : "N/A");

        //Check if loan is closed
        if(loan.getStatus() == LoanStatus.RETURNED || loan.getStatus() == LoanStatus.REJECTED){
            throw new InvalidLoanStatusChangeException("Can not change statius of closed loan");
        }else {
            loan.setStatus(actionDTO.loanStatus());

        }

        //Change asset availiblity status
        if (actionDTO.loanStatus() == LoanStatus.RETURNED) {
            loan.setReturnDate(LocalDateTime.now());

            assert asset != null;
            asset.setStatus(AssetStatus.AVAILABLE);
            assetRepository.save(asset);
        } else if (actionDTO.loanStatus() == LoanStatus.APPROVED){
            loan.setCheckoutDate(LocalDateTime.now());
            User user = userRepository.findById(getCurrentUserId()).get();
            String message = "Your Loan for asset: " +loan.getAsset().getTitle() + " was approved.";
            String email = user.getEmail();
            notificationService.sendAll(loan.getUser().getUserId(),email, NotificationReason.LOAN_STATUS_UPDATED,message);
            assert asset != null;
            asset.setStatus(AssetStatus.LOANED);
            assetRepository.save(asset);
        }


        // Explicit structural state routing to evaluate check-in or check-out lifecycles
        if (actionDTO.loanStatus() == LoanStatus.APPROVED) {
            if (asset != null) {
                asset.setStatus(AssetStatus.LOANED);
                loan.setDueDate(LocalDateTime.now().plusDays(loan.getLoanPeriod().ordinal()));
                assetRepository.save(asset);
            }
            auditLogService.logCheckOut(getCurrentUserId(), loanId, assetInfo);

        } else if (actionDTO.loanStatus() == LoanStatus.RETURNED) {
            loan.setReturnDate(LocalDateTime.now());
            if (asset != null) {
                asset.setStatus(AssetStatus.AVAILABLE);
                assetRepository.save(asset);
            }
            auditLogService.logCheckIn(getCurrentUserId(), loanId, assetInfo);

        } else {
            // General update auditing (e.g., REJECTED, PENDING updates)
            auditLogService.logUpdate(getCurrentUserId(), EntityType.LOAN, loanId, "Status: " + oldStatus, "Status: " + actionDTO.loanStatus());
        }

        return loanMapper.loanToLoanResponseDTO(loanRepository.save(loan));
    }

    public LoanResponseDTO requestLoan(LoanRequestDTO requestDTO){
        Loan loan = loanMapper.loanRequestDTOToLoan(requestDTO);
        if(loanRepository.countByUserUserIdAndStatus(loan.getUser().getUserId(),LoanStatus.APPROVED)>5){
            throw new ExceededLoanRequestException("User had too many active loans");
        }
        if (loanRepository.existsByUserUserIdAndAssetAssetIdAndStatusIn(requestDTO.userId(), requestDTO.assetId(),List.of(LoanStatus.APPROVED, LoanStatus.PENDING))) {
            throw new BadLoanRequest("User has already has an active loan for this asset");
        }

        // Audit log registration for initial request creation
        //auditLogService.logCreate(getCurrentUserId(), EntityType.LOAN, loan.getLoanId(), "Loan requested for Asset ID: " + requestDTO.assetId());

        return loanMapper.loanToLoanResponseDTO(loanRepository.save(loan));
    }

    public List<LoanResponseDTO> getAllLoansByStatus(LoanStatus status){
        return loanRepository.findByStatus(status).stream().map(loanMapper::loanToLoanResponseDTO).toList();
    }

    public List<LoanResponseDTO> getAllLoans(){
        return loanRepository.findAll().stream().
                map(loanMapper::loanToLoanResponseDTO).toList();
    }

    public List<LoanResponseDTO> getUserLoansByStatus(Long userId, LoanStatus status){
        if(!userRepository.existsById(userId)){
            throw new UserNotFoundException("User does not exist");
        }
        List<LoanResponseDTO> loans = loanRepository.findByUserUserIdAndStatus(userId, status).stream()
                .map(loanMapper::loanToLoanResponseDTO).toList();

        if(loans.isEmpty()){
            throw new NoLoansFoundException("User does not have any "+ status.toString() + " loans");
        }
        return loans;
    }




    public Long getCurrentUserId() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId(); // Returns your actual logged-in user's database ID
        }
        return null; // System or unauthenticated action
    }


}