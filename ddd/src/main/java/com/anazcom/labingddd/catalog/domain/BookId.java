package com.anazcom.labingddd.catalog.domain;

import com.anazcom.labingddd.shared.domain.Ids;
import java.util.UUID;

public record BookId(String value) {

  /**
   * Creates a new BookId instance with the specified value.
   * 
   * @param value the value for the book ID
   * 
   * @throws DomainValidationException if the value is null or blank
   */
  public BookId {
    Ids.requireNonBlank(value, "BookId");
  }

  public static BookId generate() {
    return new BookId(UUID.randomUUID().toString());
  }
}
