package com.invision.web.Invision.dto;

import com.invision.web.Invision.enums.AssetLoanStatus;
import com.invision.web.Invision.enums.LoanStatus;


import java.time.LocalDateTime;
import com.invision.web.Invision.enums.AssetLoanStatus;
import com.invision.web.Invision.enums.LoanStatus;
import java.time.LocalDateTime;

public record LoanResponseDTO(
        String loanId,
        String assetTitle,
        String borrowerName,    // 💡 Changed from description to catch the user's name
        String description,     // 💡 This matches loan.getDescription()
        LocalDateTime requestDate,
        LoanStatus status,
        LocalDateTime dueDate,
        AssetLoanStatus assetLoanStatus
) {
}
