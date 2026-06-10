package com.invision.web.Invision.dto;

import com.invision.web.Invision.enums.LoanPeriod;

import jakarta.validation.constraints.*;

public record LoanRequestDTO(
        @NotNull(message = "Asset ID is required")
        @Positive(message = "Asset ID must be valid")
        Long assetId, // Changed from long to Long

        @NotNull(message = "User ID is required")
        @Positive(message = "User ID must be valid")
        Long userId,  // Changed from long to Long

        @NotBlank(message = "Description is required")
        @Size(max = 255, message = "Description must be under 255 characters")
        String description,

        @NotNull(message = "Loan period is required")
        LoanPeriod loanPeriod
) {}
