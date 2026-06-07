package com.invision.web.Invision.service;

import com.invision.web.Invision.dto.LoanActionDTO;
import com.invision.web.Invision.dto.LoanRequestDTO;
import com.invision.web.Invision.dto.LoanResponseDTO;
import com.invision.web.Invision.mapper.LoanMapper;
import com.invision.web.Invision.enums.Department;
import com.invision.web.Invision.model.Loan;
import com.invision.web.Invision.repository.AssetRepository;
import com.invision.web.Invision.repository.LoanRepository;
import com.invision.web.Invision.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

public class LoanService {
    AssetRepository assetRepository;
    UserRepository userRepository;
    LoanRepository loanRepository;
    LoanMapper loanMapper;

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

    public List<LoanResponseDTO> getLoansUser(Long userId){
        return loanRepository.findByUserUserId(userId).stream().map(loanMapper::loanToLoanResponseDTO).toList();
    }

    public LoanResponseDTO updateLoanStatus(LoanActionDTO actionDTO){
        Loan loan = loanRepository.findById(actionDTO.loanId()).orElseThrow(()->new EntityNotFoundException("Loan not found: " + actionDTO.loanId()));
        loan.setStatus(actionDTO.loanStatus());

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



}
