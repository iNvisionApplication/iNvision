package com.invision.web.Invision.dto;

import com.invision.web.Invision.enums.LoanStatus;
import com.invision.web.Invision.enums.ActionLog;

public record LoanActionDTO(long loanId, ActionLog action, LoanStatus loanStatus) {
}
