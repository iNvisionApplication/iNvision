package com.invision.web.Invision.mapper;

import com.invision.web.Invision.dto.LoanActionDTO;
import com.invision.web.Invision.dto.LoanRequestDTO;
import com.invision.web.Invision.dto.LoanResponseDTO;
import com.invision.web.Invision.enums.AssetLoanStatus;
import com.invision.web.Invision.enums.Department;
import com.invision.web.Invision.model.Asset;
import com.invision.web.Invision.model.Loan;
import com.invision.web.Invision.enums.LoanStatus;
import com.invision.web.Invision.model.User;
import com.invision.web.Invision.repository.AssetRepository;
import com.invision.web.Invision.repository.LoanRepository;
import com.invision.web.Invision.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class LoanMapper {
    private final AssetRepository assetRepository;
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    //AssetRepository assetRepository;
    //UserRepository userRepository;
    //LoanRepository loanRepository;


    public LoanResponseDTO loanToLoanResponseDTO(Loan loan){


        return new LoanResponseDTO(String.valueOf(loan.getLoanId()),loan.getAsset().getTitle(),loan.getUser().getName(),
                loan.getRequestDate(),loan.getStatus(),loan.getLoanPeriod(),loan.getAssetLoanStatus());
    }

    public Loan loanRequestDTOToLoan(LoanRequestDTO requestDTO, Department department, Long loanId){
        Asset asset = assetRepository.findById(requestDTO.assetId())
                .orElseThrow(() -> new EntityNotFoundException("Asset not found: " + requestDTO.assetId()));


        return Loan.builder().asset(asset).requestDate(LocalDateTime.now()).
                status(LoanStatus.PENDING).description(requestDTO.description()).
                loanPeriod(requestDTO.loanPeriod()).userDepartment(department).loanId(loanId).
                assetLoanStatus(AssetLoanStatus.PENDING_APPROVAL)
                .build();
    }

//    public Loan LoanActionDTOToLoan(Long loanId, LoanActionDTO actionDTO){
//        Loan loan = loanRepository.findById(loanId)
//                .orElseThrow(() -> new EntityNotFoundException("Loan not found: " + loanId));
//
//        loan.setStatus(actionDTO.loanStatus());
//
//        return loan;
//    }

}
