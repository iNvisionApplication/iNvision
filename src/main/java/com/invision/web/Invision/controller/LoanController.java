package com.invision.web.Invision.controller;

import com.invision.web.Invision.dto.LoanActionDTO;
import com.invision.web.Invision.dto.LoanRequestDTO;
import com.invision.web.Invision.dto.LoanResponseDTO;
import com.invision.web.Invision.enums.Department;
import com.invision.web.Invision.enums.LoanStatus;
import com.invision.web.Invision.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loan")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/")
    public ResponseEntity<LoanResponseDTO> requestLoan(
            @RequestBody LoanRequestDTO loanRequestDTO,
            @RequestHeader("X-User-Id") Long userId) {

        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.requestLoan(loanRequestDTO, userId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<LoanResponseDTO> updateLoanStatus(
            @PathVariable("id") Long loanId,
            @RequestBody LoanActionDTO actionDTO,
            @RequestHeader("X-User-Id") Long userId) {

        return ResponseEntity.ok(loanService.updateLoanStatus(loanId, actionDTO, userId));
    }

    @GetMapping("/overdue_loans")
    public ResponseEntity<List<LoanResponseDTO>> getAllOverdueLoans(){
        return ResponseEntity.ok(loanService.getAllOverdueLoans());
    }

    @GetMapping("/overdue_loans/department/{department}")
    public ResponseEntity<List<LoanResponseDTO>> getOverdueLoansByDepartment(@PathVariable Department department){
        return ResponseEntity.ok(loanService.getOverdueLoansByDepartment(department));
    }

    @GetMapping("/asset/{assetId}")
    public ResponseEntity<List<LoanResponseDTO>> getOverdueLoansByAsset(@PathVariable Long assetId){
        return ResponseEntity.ok(loanService.getLoansByAsset(assetId));
    }

    @GetMapping("/user/{userId}/status")
    public ResponseEntity<List<LoanResponseDTO>> getUserLoansByStatus(@PathVariable Long userId, @RequestParam LoanStatus status){
        return ResponseEntity.ok(loanService.getUserLoansByStatus(userId,status));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LoanResponseDTO>> getUserLoans(@PathVariable Long userId){
        return ResponseEntity.ok(loanService.getUserLoans(userId));
    }

    @GetMapping("/")
    public ResponseEntity<List<LoanResponseDTO>> getAllLoans(){
        return ResponseEntity.ok(loanService.getAllLoans());
    }






}
