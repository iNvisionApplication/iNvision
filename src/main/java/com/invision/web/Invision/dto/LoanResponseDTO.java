package com.invision.web.Invision.dto;

import com.invision.web.Invision.model.AssetStatus;

import java.time.LocalDateTime;

public record LoanResponseDTO(String loanId, String assetTitle, String borrowerName, LocalDateTime requestDate,
                              AssetStatus status,LocalDateTime dueDate) {
}
