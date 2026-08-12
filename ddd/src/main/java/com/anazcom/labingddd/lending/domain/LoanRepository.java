package com.anazcom.labingddd.lending.domain;

import com.anazcom.labingddd.catalog.domain.BookId;

public interface LoanRepository {
  void save(Loan loan);

  boolean hasActiveLoanForBook(BookId bookId);
}
