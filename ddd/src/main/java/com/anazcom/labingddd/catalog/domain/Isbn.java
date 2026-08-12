package com.anazcom.labingddd.catalog.domain;

import com.anazcom.labingddd.shared.domain.DomainValidationException;
import com.anazcom.labingddd.shared.domain.Ids;

public record Isbn(String value) {
  public Isbn {
    Ids.requireNonBlank(value, "ISBN");
    if (!value.matches("\\d{" + BookConstrains.ISBN_MAX_LENGTH + "}"))
      throw new DomainValidationException("Invalid ISBN: " + value);
  }
}
