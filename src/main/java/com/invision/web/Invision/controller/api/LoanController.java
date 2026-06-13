package com.invision.web.Invision.controller.api;

import com.invision.web.Invision.dto.LoanActionDTO;
import com.invision.web.Invision.dto.LoanRequestDTO;
import com.invision.web.Invision.dto.LoanResponseDTO;
import com.invision.web.Invision.dto.LoanStatusDTO;
import com.invision.web.Invision.enums.AssetLoanStatus;
import com.invision.web.Invision.enums.Department;
import com.invision.web.Invision.enums.LoanStatus;
import com.invision.web.Invision.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    public ResponseEntity<LoanResponseDTO> requestLoan(
            @RequestBody LoanRequestDTO loanRequestDTO) {

        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.requestLoan(loanRequestDTO));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<LoanResponseDTO> updateLoanStatus(
            @PathVariable("id") Long loanId,
            @RequestBody LoanStatusDTO actionDTO) {

        return ResponseEntity.ok(loanService.updateLoanStatus(loanId, actionDTO));
    }

    @PatchMapping("/{id}/return")
    public ResponseEntity<LoanResponseDTO> LoanActionReturn(@PathVariable("id") Long loanId){
       return ResponseEntity.ok(loanService.loanActionReturn(loanId));
    }

    @PatchMapping("/{id}/collect")
    public ResponseEntity<LoanResponseDTO> LoanActionCollect(@PathVariable("id") Long loanId){
        return ResponseEntity.ok(loanService.loanActionCollect(loanId));
    }

    @GetMapping("/status")
    public ResponseEntity<List<LoanResponseDTO>> getLoansByStatus(@RequestBody LoanStatus status){
        return ResponseEntity.ok(loanService.getAllLoansByStatus(status));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<LoanResponseDTO>> getAllOverdueLoans(){
        return ResponseEntity.ok(loanService.getAllOverdueLoans());
    }

    @GetMapping("/overdue/department/{department}")
    public ResponseEntity<List<LoanResponseDTO>> getOverdueLoansByDepartment(@PathVariable Department department){
        return ResponseEntity.ok(loanService.getOverdueLoansByDepartment(department));
    }

    @GetMapping("/overdue/{userId}")
    public ResponseEntity<List<LoanResponseDTO>> getUserOverDueLoans(@PathVariable Long userId){
        return ResponseEntity.ok(loanService.getUserOverdueLoans(userId));
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

    @GetMapping
    public ResponseEntity<List<LoanResponseDTO>> getAllLoans(){
        return ResponseEntity.ok(loanService.getAllLoans());
    }


}
