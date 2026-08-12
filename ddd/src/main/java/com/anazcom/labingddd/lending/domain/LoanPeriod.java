package com.anazcom.labingddd.lending.domain;

import java.time.LocalDate;

import com.anazcom.labingddd.shared.domain.DomainValidationException;

public record LoanPeriod(LocalDate startDate, LocalDate dueDate) {

  public LoanPeriod {
    if (startDate == null || dueDate == null) {
      throw new DomainValidationException("startDate and dueDate cannot be null");
    }
    if (startDate.compareTo(dueDate) > 0) {
      throw new DomainValidationException("startDate cannot be greater than dueDate");
    }
  }

  public static LoanPeriod standard(LocalDate now) {
    return new LoanPeriod(now, now.plusWeeks(2));
  }
}
