package com.invision.web.Invision.controller.api;

import com.invision.web.Invision.dto.AssetResponseDTO;
import com.invision.web.Invision.dto.LoanReportDTO;
import com.invision.web.Invision.model.Loan;
import com.invision.web.Invision.repository.LoanRepository;
import com.invision.web.Invision.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
public class ReportApiController {

    private final AssetService assetService;
    private final LoanRepository loanRepository; // Inject repository directly for raw entity streams

    @GetMapping("/inventory")
    public ResponseEntity<List<AssetResponseDTO>> getInventoryReport(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String condition) {

        List<AssetResponseDTO> reportData = assetService.searchAndFilterAssets(title, category, status, location, condition);
        return ResponseEntity.ok(reportData);
    }

    @GetMapping("/loans")
    public ResponseEntity<List<LoanReportDTO>> getLoanHistoryReport(
            @RequestParam(required = false) String assetTitle,
            @RequestParam(required = false) String borrowerEmail,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status) {

        List<Loan> allLoans = loanRepository.findAll();

        List<LoanReportDTO> filteredReport = allLoans.stream()
                .filter(l -> assetTitle == null || assetTitle.trim().isEmpty() ||
                        (l.getAsset() != null && l.getAsset().getTitle().toLowerCase().contains(assetTitle.toLowerCase().trim())))

                .filter(l -> borrowerEmail == null || borrowerEmail.trim().isEmpty() ||
                        (l.getUser() != null && l.getUser().getEmail().toLowerCase().contains(borrowerEmail.toLowerCase().trim())))

                .filter(l -> department == null || department.trim().isEmpty() ||
                        (l.getUserDepartment() != null && l.getUserDepartment().name().equalsIgnoreCase(department.trim())))

                .filter(l -> status == null || status.trim().isEmpty() ||
                        (l.getStatus() != null && l.getStatus().name().equalsIgnoreCase(status.trim())))
                .map(l -> new LoanReportDTO(
                        l.getLoanId(),
                        l.getAsset() != null ? l.getAsset().getTitle() : "Deleted Asset",
                        l.getUser() != null ? l.getUser().getEmail() : "Unknown User",
                        l.getUserDepartment() != null ? l.getUserDepartment().name() : "N/A",
                        l.getRequestDate(),
                        l.getDueDate(),
                        l.getReturnDate(),
                        l.getCheckoutDate(),
                        l.getStatus().name()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(filteredReport);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<LoanReportDTO>> getOverdueReport() {
        // Pull actual overdue database records matching structural timelines
        List<Loan> overdueLoans = loanRepository.findByDueDateBeforeAndStatusNot(LocalDateTime.now(), com.invision.web.Invision.enums.LoanStatus.RETURNED);

        List<LoanReportDTO> report = overdueLoans.stream()
                .map(l -> new LoanReportDTO(
                        l.getLoanId(),
                        l.getAsset() != null ? l.getAsset().getTitle() : "Deleted Asset",
                        l.getUser() != null ? l.getUser().getEmail() : "Unknown User",
                        l.getUserDepartment() != null ? l.getUserDepartment().name() : "N/A",
                        l.getRequestDate(),
                        l.getDueDate(),
                        l.getReturnDate(),
                        l.getCheckoutDate(),
                        l.getStatus().name()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(report);
    }
}