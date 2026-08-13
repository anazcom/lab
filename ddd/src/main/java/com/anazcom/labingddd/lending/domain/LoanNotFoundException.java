package com.anazcom.labingddd.lending.domain;

public class LoanNotFoundException extends RuntimeException {
  public LoanNotFoundException(LoanId loanId) {
    super("Loan not found: " + loanId.value());
  }

}
