package com.invision.web.Invision.exception.loan;

public class BadLoanRequest extends RuntimeException {
  public BadLoanRequest(String message) {
    super(message);
  }
}
