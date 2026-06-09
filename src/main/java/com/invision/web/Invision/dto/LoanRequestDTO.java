package com.invision.web.Invision.dto;

import java.time.LocalDateTime;

public record LoanRequestDTO(long assetId, long userId , String description, LocalDateTime dueDate) {
}
