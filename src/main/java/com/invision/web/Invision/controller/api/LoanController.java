package com.invision.web.Invision.controller.api;

import com.invision.web.Invision.dto.*;
import com.invision.web.Invision.enums.AssetLoanStatus;
import com.invision.web.Invision.enums.Department;
import com.invision.web.Invision.enums.LoanStatus;
import com.invision.web.Invision.enums.Location;
import com.invision.web.Invision.model.Asset;
import com.invision.web.Invision.model.Loan;
import com.invision.web.Invision.repository.AssetRepository;
import com.invision.web.Invision.repository.LoanRepository;
import com.invision.web.Invision.service.LoanService;
import jakarta.persistence.EntityNotFoundException;
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
    private final LoanRepository loanRepository;
    private final AssetRepository assetRepository;

    @PostMapping
    public ResponseEntity<LoanResponseDTO> requestLoan(
            @RequestBody LoanRequestDTO loanRequestDTO) {
        System.out.println("dates from front end:"+ loanRequestDTO.dueDate()+",,, "+loanRequestDTO.checkoutDate());
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

    @PatchMapping("/reject")
    public ResponseEntity<LoanResponseDTO> rejectLoan(
            @RequestBody LoanRejectionDTO rejectionDTO) throws BadRequestException {

        return ResponseEntity.ok(loanService.rejectLoan(rejectionDTO));
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

    @PutMapping("/{loanId}/return-verification")
    public ResponseEntity<?> verifyAssetReturn(
            @PathVariable Long loanId,
            @RequestBody ReturnVerificationDTO verificationData) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Loan not found with ID: " + loanId));

        // Update the Asset's condition and location based on verificationData
        Asset asset = loan.getAsset();
        asset.setCondition(verificationData.condition());
        asset.setLocation(verificationData.location());

        // Mark the Loan status as Returned
        loan.setAssetLoanStatus(AssetLoanStatus.RETURN_CONFIRMED);
        loan.setStatus(LoanStatus.RETURNED);

        // Desc for the condition by the manager
        asset.setDescription(verificationData.managerNotes());

        // Save changes
        assetRepository.save(asset);
        loanRepository.save(loan);

        return ResponseEntity.ok().build();
    }
}
