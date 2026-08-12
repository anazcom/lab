package com.anazcom.labingddd.shared.domain;

public final class Ids {

  public static void requireNonBlank(String value, String idName) {
    if (value == null || value.isBlank()) {
      throw new DomainValidationException("Invalid " + idName + ": " + value);
    }
  }
}
