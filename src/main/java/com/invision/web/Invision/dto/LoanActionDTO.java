package com.invision.web.Invision.dto;

import com.invision.web.Invision.enums.ActionLog;
import com.invision.web.Invision.enums.LoanStatus;

public record LoanActionDTO(long loanId, ActionLog action, LoanStatus loanStatus) {
}
