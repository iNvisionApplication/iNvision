package com.invision.web.Invision.dto;

import com.invision.web.Invision.enums.Department;
import com.invision.web.Invision.enums.LoanPeriod;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;

public record LoanRequestDTO(
        @NotNull(message = "Asset ID is required")
        @Positive(message = "Asset ID must be valid")
        Long assetId,



        @NotBlank(message = "Description is required")
        @Size(max = 20, message = "Description must be under 255 characters")
        String description,

        @NotNull(message = "Loan period is required")
        LoanPeriod loanPeriod,

        @Enumerated(EnumType.STRING)
        Department department
) {}
