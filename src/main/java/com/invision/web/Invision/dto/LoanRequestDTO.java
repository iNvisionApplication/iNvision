package com.invision.web.Invision.dto;

import com.invision.web.Invision.enums.LoanPeriod;

import java.time.LocalDateTime;

public record LoanRequestDTO(long assetId, long userId , String description, LoanPeriod loanPeriod) {
}
