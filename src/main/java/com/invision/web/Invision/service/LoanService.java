package com.invision.web.Invision.service;

import com.invision.web.Invision.config.CustomUserDetails;
import com.invision.web.Invision.dto.LoanActionDTO;
import com.invision.web.Invision.dto.LoanRequestDTO;
import com.invision.web.Invision.dto.LoanResponseDTO;
import com.invision.web.Invision.enums.AssetStatus;
import com.invision.web.Invision.enums.EntityType;
import com.invision.web.Invision.enums.LoanStatus;
import com.invision.web.Invision.mapper.LoanMapper;
import com.invision.web.Invision.enums.Department;
import com.invision.web.Invision.model.Asset;
import com.invision.web.Invision.model.Loan;
import com.invision.web.Invision.model.User;
import com.invision.web.Invision.repository.AssetRepository;
import com.invision.web.Invision.repository.LoanRepository;
import com.invision.web.Invision.service.AuditLogService;
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

    public List<LoanResponseDTO> getAllOverdueLoans(){
        return loanRepository.findAll().stream()
                .filter(Loan::isOverdue)
                .map(loanMapper::loanToLoanResponseDTO).toList();
    }

    public List<LoanResponseDTO> getOverdueLoansByDepartment(Department department){
        return loanRepository.findAll().stream()
                .filter(loan -> loan.getUser().getDepartment() == department)
                .filter(Loan::isOverdue)
                .map(loanMapper::loanToLoanResponseDTO).toList();
    }

    public List<LoanResponseDTO> getLoansByAsset(Long assetId){
        return loanRepository.findByAssetAssetId(assetId).stream().map(loanMapper::loanToLoanResponseDTO).toList();
    }

    public List<LoanResponseDTO> getUserLoans(Long userId){
        return loanRepository.findByUserUserId(userId).stream().map(loanMapper::loanToLoanResponseDTO).toList();
    }

    public LoanResponseDTO updateLoanStatus(Long loanId, LoanActionDTO actionDTO){
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Loan not found: " + loanId));

        LoanStatus oldStatus = loan.getStatus();
        loan.setStatus(actionDTO.loanStatus());


        Asset asset = loan.getAsset();
        String assetInfo = "Asset ID: " + (asset != null ? asset.getAssetId() : "N/A");

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
        loanRepository.save(loan);

        // Audit log registration for initial request creation
        //auditLogService.logCreate(getCurrentUserId(), EntityType.LOAN, loan.getLoanId(), "Loan requested for Asset ID: " + requestDTO.assetId());

        return loanMapper.loanToLoanResponseDTO(loan);
    }

    public List<LoanResponseDTO> getAllLoans(){
        return loanRepository.findAll().stream()
                .map(loanMapper::loanToLoanResponseDTO).toList();
    }

    public List<LoanResponseDTO> getUserLoansByStatus(Long userId, LoanStatus status){
        return loanRepository.findByUserUserIdAndStatus(userId, status).stream().map(loanMapper::loanToLoanResponseDTO).toList();
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

