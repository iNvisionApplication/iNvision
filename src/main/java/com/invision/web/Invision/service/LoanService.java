package com.invision.web.Invision.service;

import com.invision.web.Invision.dto.LoanActionDTO;
import com.invision.web.Invision.dto.LoanRequestDTO;
import com.invision.web.Invision.dto.LoanResponseDTO;
import com.invision.web.Invision.enums.AssetStatus;
import com.invision.web.Invision.enums.LoanStatus;
import com.invision.web.Invision.mapper.LoanMapper;
import com.invision.web.Invision.enums.Department;
import com.invision.web.Invision.model.Asset;
import com.invision.web.Invision.model.Loan;
import com.invision.web.Invision.repository.AssetRepository;
import com.invision.web.Invision.repository.LoanRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class LoanService {
    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;
    private final AssetRepository assetRepository;

    public List<LoanResponseDTO> getAllOverdueLoans(){
        return loanRepository.findAll().stream().
                filter(Loan::isOverdue).
                map(loanMapper::loanToLoanResponseDTO).toList();
    }

    public List<LoanResponseDTO> getOverdueLoansByDepartment(Department department){
        return loanRepository.findAll().stream().filter(loan -> loan.getUser().getDepartment() == department).
                filter(Loan::isOverdue).
                map(loanMapper::loanToLoanResponseDTO).toList();
    }

    public List<LoanResponseDTO> getLoansByAsset(Long assetId){
        return loanRepository.findByAssetAssetId(assetId).stream().map(loanMapper::loanToLoanResponseDTO).toList();
    }

    public List<LoanResponseDTO> getUserLoans(Long userId){
        return loanRepository.findByUserUserId(userId).stream().map(loanMapper::loanToLoanResponseDTO).toList();
    }

    public LoanResponseDTO updateLoanStatus(Long loanId, LoanActionDTO actionDTO){
        Loan loan = loanRepository.findById(loanId).orElseThrow(()->new EntityNotFoundException("Loan not found: " +loanId ));
        loan.setStatus(actionDTO.loanStatus());

        if (actionDTO.loanStatus() == LoanStatus.RETURNED) {
            loan.setReturnDate(LocalDateTime.now());
            Asset asset = loan.getAsset();
            asset.setStatus(AssetStatus.AVAILABLE);
            assetRepository.save(asset);
        }



        return loanMapper.loanToLoanResponseDTO(loanRepository.save(loan));
    }

    public LoanResponseDTO requestLoan(LoanRequestDTO requestDTO){
        Loan loan = loanMapper.loanRequestDTOToLoan(requestDTO);
        return loanMapper.loanToLoanResponseDTO(loanRepository.save(loan));
    }

    public List<LoanResponseDTO> getAllLoans(){
        return loanRepository.findAll().stream().
                map(loanMapper::loanToLoanResponseDTO).toList();
    }

    public List<LoanResponseDTO> getUserLoansByStatus(Long userId, LoanStatus status){
        return loanRepository.findByUserUserIdAndStatus(userId,status).stream().map(loanMapper::loanToLoanResponseDTO).toList();
    }



}
