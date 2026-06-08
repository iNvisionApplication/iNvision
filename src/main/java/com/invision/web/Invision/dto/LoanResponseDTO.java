package com.invision.web.Invision.dto;

import com.invision.web.Invision.enums.LoanStatus;


import java.time.LocalDateTime;

public record LoanResponseDTO(String loanId, String assetTitle, String borrowerName, LocalDateTime requestDate,
                              LoanStatus status, LocalDateTime dueDate) {
}
