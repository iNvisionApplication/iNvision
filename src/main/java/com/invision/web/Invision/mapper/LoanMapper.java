package com.invision.web.Invision.mapper;

import com.invision.web.Invision.dto.LoanActionDTO;
import com.invision.web.Invision.dto.LoanRequestDTO;
import com.invision.web.Invision.dto.LoanResponseDTO;
import com.invision.web.Invision.model.Asset;
import com.invision.web.Invision.model.Loan;
import com.invision.web.Invision.enums.LoanStatus;
import com.invision.web.Invision.model.User;
import com.invision.web.Invision.repository.AssetRepository;
import com.invision.web.Invision.repository.LoanRepository;
import com.invision.web.Invision.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;

public class LoanMapper {
    AssetRepository assetRepository;
    LoanRepository loanRepository;
    UserRepository userRepository;

    AssetRepository assetRepository;
    UserRepository userRepository;
    LoanRepository loanRepository;

    public LoanResponseDTO loanToLoanResponseDTO(Loan loan){
        return new LoanResponseDTO(String.valueOf(loan.getLoanId()),loan.getAsset().getTitle(),loan.getUser().getName(),
                loan.getRequestDate(),loan.getStatus(),loan.getDueDate());
    }

    public Loan loanRequestDTOToLoan(LoanRequestDTO requestDTO){
        Asset asset = assetRepository.findById(requestDTO.assetId())
                .orElseThrow(() -> new EntityNotFoundException("Asset not found: " + requestDTO.assetId()));

        User user = userRepository.findById(requestDTO.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + requestDTO.userId()));

        return Loan.builder().asset(asset).user(user).requestDate(LocalDateTime.now()).status(LoanStatus.PENDING)
                .build();
    }

    public Loan LoanActionDTOToLoan(LoanActionDTO actionDTO){
        Loan loan = loanRepository.findById(actionDTO.loanId())
                .orElseThrow(() -> new EntityNotFoundException("Loan not found: " + actionDTO.loanId()));

        loan.setStatus(actionDTO.loanStatus());

        return loan;
    }

}
