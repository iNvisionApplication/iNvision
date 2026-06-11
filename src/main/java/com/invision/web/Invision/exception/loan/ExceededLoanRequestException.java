package com.invision.web.Invision.exception.loan;

public class ExceededLoanRequestException extends RuntimeException {
    public ExceededLoanRequestException(String message) {
        super(message);
    }
}
