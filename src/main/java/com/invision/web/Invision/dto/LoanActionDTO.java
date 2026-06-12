package com.invision.web.Invision.dto;

import com.invision.web.Invision.model.ActionLog;

public record LoanActionDTO(long loanId, ActionLog action) {
}
