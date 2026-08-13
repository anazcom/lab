package com.anazcom.labingddd.lending.domain;

import java.util.Optional;

import com.anazcom.labingddd.catalog.domain.BookId;

public interface LoanRepository {
  void save(Loan loan);

  boolean hasActiveLoanForBook(BookId bookId);

  Optional<Loan> findById(LoanId loanId);
}
