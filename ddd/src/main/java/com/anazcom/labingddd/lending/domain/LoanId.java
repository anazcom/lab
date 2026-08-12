package com.anazcom.labingddd.lending.domain;

import com.anazcom.labingddd.shared.domain.Ids;
import java.util.UUID;

public record LoanId(String value) {

  public LoanId {
    Ids.requireNonBlank(value, "LoanId");
  }

  public static LoanId generate() {
    return new LoanId(UUID.randomUUID().toString());
  }
}
