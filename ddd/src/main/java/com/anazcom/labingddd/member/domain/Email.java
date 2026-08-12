package com.anazcom.labingddd.member.domain;

import com.anazcom.labingddd.shared.domain.DomainValidationException;
import com.anazcom.labingddd.shared.domain.Ids;

public record Email(String value) {

  private static final String EMAIL_PATTERN = "^[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}$";

  public Email {
    Ids.requireNonBlank(value, "Email");
    if (!value.matches(EMAIL_PATTERN)) {
      throw new DomainValidationException("Not a valid email");
    }
  }
}
