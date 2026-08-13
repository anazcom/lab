package com.anazcom.labingddd.lending.domain;

import com.anazcom.labingddd.catalog.domain.BookId;
import com.anazcom.labingddd.member.domain.MemberId;
import com.anazcom.labingddd.shared.domain.DomainStateException;

import java.time.LocalDate;

public class Loan {
  private final LoanId id;
  private final BookId bookId;
  private final MemberId memberId;
  private LoanStatus status;
  private LoanPeriod period;

  public Loan(LoanId id, BookId bookId, MemberId memberId, LoanPeriod period, LoanStatus loanStatus) {
    this.id = id;
    this.bookId = bookId;
    this.memberId = memberId;
    this.period = period;
    this.status = loanStatus;
  }

  public Loan(LoanId id, BookId bookId, MemberId memberId, LoanPeriod period) {
    this(id, bookId, memberId, period, LoanStatus.ACTIVE);
  }

  public LoanId getId() {
    return id;
  }

  public BookId getBookId() {
    return bookId;
  }

  public MemberId getMemberId() {
    return memberId;
  }

  public LoanStatus getStatus() {
    return status;
  }

  public boolean isOverdue() {
    return this.status == LoanStatus.ACTIVE && LocalDate.now().isAfter(period.dueDate());
  }

  public LoanPeriod getPeriod() {
    return period;
  }

  public void changePeriod(LoanPeriod period) {
    if (status == LoanStatus.RETURNED) {
      throw new DomainStateException("Cannot change period on returned loan");
    }
    this.period = period;
  }

  public void markReturned() {
    if (status == LoanStatus.RETURNED) {
      throw new DomainStateException("Loan already returned");
    }
    this.status = LoanStatus.RETURNED;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Loan other))
      return false;
    return this.id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return this.id.hashCode();
  }
}
