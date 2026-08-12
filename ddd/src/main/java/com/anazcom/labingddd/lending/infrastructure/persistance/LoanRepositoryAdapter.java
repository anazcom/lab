package com.anazcom.labingddd.lending.infrastructure.persistance;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import com.anazcom.labingddd.catalog.domain.BookId;
import com.anazcom.labingddd.lending.domain.Loan;
import com.anazcom.labingddd.lending.domain.LoanRepository;
import com.anazcom.labingddd.lending.domain.LoanStatus;
import com.anazcom.labingddd.shared.domain.DomainRepositoryException;

@Repository
class LoanRepositoryAdapter implements LoanRepository {
  private final SpringDataLoanRepository loans;

  LoanRepositoryAdapter(SpringDataLoanRepository loans) {
    this.loans = loans;
  }

  @Override
  public void save(Loan loan) {
    LoanJpaEntity entity = new LoanJpaEntity();
    entity.setId(loan.getId().value());
    entity.setBookId(loan.getBookId().value());
    entity.setMemberId(loan.getMemberId().value());
    entity.setStatus(loan.getStatus());
    entity.setStartDate(loan.getPeriod().startDate());
    entity.setDueDate(loan.getPeriod().dueDate());

    try {
      this.loans.save(entity);
    } catch (DataIntegrityViolationException exc) {
      throw new DomainRepositoryException("Failed to save loan", exc);
    }
  }

  @Override
  public boolean hasActiveLoanForBook(BookId bookId) {
    return loans.existsByBookIdAndStatus(bookId.value(), LoanStatus.ACTIVE);
  }
}
