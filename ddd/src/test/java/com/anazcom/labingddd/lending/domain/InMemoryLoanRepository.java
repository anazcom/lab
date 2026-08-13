package com.anazcom.labingddd.lending.domain;

import com.anazcom.labingddd.catalog.domain.BookId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryLoanRepository implements LoanRepository {
  private final Map<LoanId, Loan> loans = new HashMap<>();

  @Override
  public boolean hasActiveLoanForBook(BookId bookId) {
    return loans.values().stream()
        .anyMatch(
            loan -> loan.getBookId().equals(bookId) && loan.getStatus() != LoanStatus.RETURNED);
  }

  @Override
  public void save(Loan loan) {
    this.loans.put(loan.getId(), loan);
  }

  @Override
  public Optional<Loan> findById(LoanId loanId) {
    return Optional.ofNullable(this.loans.get(loanId));
  }

  public void clear() {
    this.loans.clear();
  }

}
