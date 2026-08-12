package com.anazcom.labingddd.lending.domain;

import com.anazcom.labingddd.catalog.domain.BookId;

public interface BookCatalog {
  boolean exists(BookId bookId);
}
