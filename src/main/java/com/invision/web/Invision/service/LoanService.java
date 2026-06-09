package com.invision.web.Invision.service;

import com.invision.web.Invision.dto.LoanActionDTO;
import com.invision.web.Invision.dto.LoanRequestDTO;
import com.invision.web.Invision.dto.LoanResponseDTO;
import com.invision.web.Invision.enums.AssetStatus;
import com.invision.web.Invision.enums.LoanStatus;
import com.invision.web.Invision.exception.loan.BadLoanRequest;
import com.invision.web.Invision.exception.loan.InvalidLoanStatusChangeException;
import com.invision.web.Invision.exception.user.UserNotFoundException;
import com.invision.web.Invision.mapper.LoanMapper;
import com.invision.web.Invision.enums.Department;
import com.invision.web.Invision.model.Asset;
import com.invision.web.Invision.model.Loan;
import com.invision.web.Invision.model.NoLoansFoundException;
import com.invision.web.Invision.repository.AssetRepository;
import com.invision.web.Invision.repository.LoanRepository;
import com.invision.web.Invision.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class LoanService {
    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;

    public List<LoanResponseDTO> getAllOverdueLoans(){
        return loanRepository.findAll().stream().
                filter(Loan::isOverdue).
                map(loanMapper::loanToLoanResponseDTO).toList();
    }

    public List<LoanResponseDTO> getOverdueLoansByDepartment(Department department){
        List<Loan> loans = loanRepository.findAll();
        if(loans.isEmpty()){
            throw new NoLoansFoundException("There are no overdueLoans");
        }
        return loans.stream().filter(loan -> loan.getUser().getDepartment() == department).
                filter(Loan::isOverdue).
                map(loanMapper::loanToLoanResponseDTO).toList();
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
        Loan loan = loanRepository.findById(loanId).orElseThrow(()->new NoLoansFoundException("Loan not found: " +loanId ));
        Asset asset = loan.getAsset();

        //Check if loan is closed
        if(loan.getStatus() == LoanStatus.RETURNED || loan.getStatus() == LoanStatus.REJECTED){
            throw new InvalidLoanStatusChangeException("Can not change statius of closed loan");
        }else {
            loan.setStatus(actionDTO.loanStatus());

        }

        //Change asset availiblity status
        if (actionDTO.loanStatus() == LoanStatus.RETURNED) {
            loan.setReturnDate(LocalDateTime.now());

            asset.setStatus(AssetStatus.AVAILABLE);
            assetRepository.save(asset);
        } else if (actionDTO.loanStatus() == LoanStatus.APPROVED){
            loan.setCheckoutDate(LocalDateTime.now());

            asset.setStatus(AssetStatus.LOANED);
            assetRepository.save(asset);
        }

        return loanMapper.loanToLoanResponseDTO(loanRepository.save(loan));
    }

    public LoanResponseDTO requestLoan(LoanRequestDTO requestDTO){
        Loan loan = loanMapper.loanRequestDTOToLoan(requestDTO);

        if (loanRepository.existsByUserUserIdAndAssetAssetIdAndStatusIn(requestDTO.userId(), requestDTO.assetId(),List.of(LoanStatus.APPROVED, LoanStatus.PENDING))) {
            throw new BadLoanRequest("User has already has an active loan for this asset");
        }

        return loanMapper.loanToLoanResponseDTO(loanRepository.save(loan));
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



}
