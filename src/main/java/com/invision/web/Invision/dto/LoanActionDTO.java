package com.invision.web.Invision.dto;


import com.invision.web.Invision.model.LoanStatus;

public record LoanActionDTO(long loanId, LoanStatus loanStatus) {
}
