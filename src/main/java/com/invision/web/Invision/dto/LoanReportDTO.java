package com.invision.web.Invision.dto;

import java.time.LocalDateTime;

public record LoanReportDTO(
        Long loanId,
        String assetTitle,
        String userEmail,
        String userDepartment,
        LocalDateTime requestDate,
        LocalDateTime dueDate,
        LocalDateTime returnDate,
        LocalDateTime checkoutDate,
        String status
) {}
