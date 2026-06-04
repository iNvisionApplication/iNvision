package com.invision.web.Invision.dto;

import com.invision.web.Invision.model.Status;

import java.time.LocalDateTime;

public record LoanResponseDTO(String loanId, String assetTitle, String borrowerName, LocalDateTime requestDate,
                              Status status,LocalDateTime dueDate) {
}
