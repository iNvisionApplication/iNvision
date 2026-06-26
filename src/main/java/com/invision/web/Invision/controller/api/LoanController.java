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
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
            @RequestBody LoanStatusDTO actionDTO) throws BadRequestException {

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
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    public ResponseEntity<Page<LoanResponseDTO>> getLoansByStatus(
            @RequestParam LoanStatus status,
            //Pending requests page should also use pagination
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size) {

        return ResponseEntity.ok(
                loanService.getAllLoansByStatus(status, page, size)
        );
    }

    @GetMapping("/department")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<org.springframework.data.domain.Page<LoanResponseDTO>> getDepartmentLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(loanService.getDepartmentLoans(page, size));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<LoanResponseDTO>> getAllOverdueLoans(){
        return ResponseEntity.ok(loanService.getAllOverdueLoans());
    }

    @GetMapping("/overdue/department/{department}")
    public ResponseEntity<List<LoanResponseDTO>> getOverdueLoansByDepartment(@PathVariable Department department){
        return ResponseEntity.ok(loanService.getOverdueLoansByDepartment(department));
    }

    //Manager and Admin
    @GetMapping("/overdue/{userId}")
    public ResponseEntity<List<LoanResponseDTO>> getUserOverDueLoans(@PathVariable Long userId){
        return ResponseEntity.ok(loanService.getUserOverdueLoans(userId));
    }

    @GetMapping("/{status}")
    public ResponseEntity<List<LoanResponseDTO>> getDepartmentLoansByStatus(@PathVariable LoanStatus status){
        return ResponseEntity.ok(loanService.getDepartmentLoansByStatus(status));
    }

    //Borrower
    @GetMapping("/my_loans/overdue")
    public ResponseEntity<List<LoanResponseDTO>> getCurrentUserOverdueLoans(){
        return ResponseEntity.ok(loanService.getCurrentUserOverdueLoans());
    }

    @GetMapping("/asset/{assetId}")
    public ResponseEntity<List<LoanResponseDTO>> getOverdueLoansByAsset(@PathVariable Long assetId){
        return ResponseEntity.ok(loanService.getLoansByAsset(assetId));
    }

    @GetMapping("/user/status")
    public ResponseEntity<List<LoanResponseDTO>> getUserLoansByStatus( @RequestParam LoanStatus status){
        return ResponseEntity.ok(loanService.getUserLoansByStatus(status));
    }

    @GetMapping("/user")
    public ResponseEntity<List<LoanResponseDTO>> getUserLoans(){
        return ResponseEntity.ok(loanService.getUserLoans());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    public ResponseEntity<Page<LoanResponseDTO>> getAllLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(loanService.getAllLoans(page, size));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<LoanResponseDTO>> getUserLoans(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(loanService.getUserLoans(userId, page, size));
    }

    @PutMapping("/{loanId}/collect")
    @PreAuthorize("hasRole('ROLE_BORROWER')")
    public ResponseEntity<LoanResponseDTO> collectLoan(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.loanActionCollect(loanId));
    }

    @PutMapping("/{loanId}/return")
    @PreAuthorize("hasRole('ROLE_BORROWER')")
    public ResponseEntity<LoanResponseDTO> initiateReturn(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.loanActionReturn(loanId));
    }

    @PutMapping("/{loanId}/confirm-return")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<LoanResponseDTO> confirmReturn(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.confirmLoanReturn(loanId));
    }

}
