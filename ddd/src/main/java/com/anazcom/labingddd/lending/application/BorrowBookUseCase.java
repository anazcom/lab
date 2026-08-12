package com.anazcom.labingddd.lending.application;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.anazcom.labingddd.catalog.domain.BookId;
import com.anazcom.labingddd.lending.domain.BookAlreadyOnLoanException;
import com.anazcom.labingddd.lending.domain.BookCatalog;
import com.anazcom.labingddd.lending.domain.Loan;
import com.anazcom.labingddd.lending.domain.LoanId;
import com.anazcom.labingddd.lending.domain.LoanPeriod;
import com.anazcom.labingddd.lending.domain.LoanRepository;
import com.anazcom.labingddd.lending.domain.MemberBorrowingProfile;
import com.anazcom.labingddd.lending.domain.MemberCannotBorrowException;
import com.anazcom.labingddd.lending.domain.MemberDirectory;
import com.anazcom.labingddd.member.domain.MemberId;
import com.anazcom.labingddd.shared.domain.DomainValidationException;

@Service
public class BorrowBookUseCase {
  private final BookCatalog books;
  private final MemberDirectory members;
  private final LoanRepository loans;

  public BorrowBookUseCase(BookCatalog books, MemberDirectory members, LoanRepository loans) {
    this.books = books;
    this.members = members;
    this.loans = loans;
  }

  /**
   * Borrows a book for a member.
   *
   * @param bookId   the ID of the book to borrow
   * @param memberId the ID of the member borrowing the book
   *
   * @return the ID of the newly created loan
   *
   * @throws DomainValidationException   if the book or member ID does not
   *                                     reference an existing book or member
   * @throws MemberCannotBorrowException if the member is not allowed to borrow
   *                                     books
   * @throws BookAlreadyOnLoanException  if the book is already on loan
   */
  public LoanId handle(BookId bookId, MemberId memberId) {

    if (!books.exists(bookId)) {
      throw new DomainValidationException("Book not found: " + bookId.value());
    }

    final MemberBorrowingProfile profile = this.members.findBorrowingProfile(memberId)
        .orElseThrow(() -> new DomainValidationException("Member not found: " + memberId.value()));

    if (!profile.canBorrow()) {
      throw new MemberCannotBorrowException(memberId);
    }

    if (loans.hasActiveLoanForBook(bookId)) {
      throw new BookAlreadyOnLoanException(bookId);
    }

    final LoanPeriod period = LoanPeriod.standard(LocalDate.now());
    final Loan loan = new Loan(LoanId.generate(), bookId, memberId, period);

    loans.save(loan);
    return loan.getId();
  }
}
