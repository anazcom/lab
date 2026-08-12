package com.anazcom.labingddd.lending.application;

import com.anazcom.labingddd.catalog.domain.BookId;
import com.anazcom.labingddd.lending.domain.BookAlreadyOnLoanException;
import com.anazcom.labingddd.lending.domain.InMemoryBookCatalog;
import com.anazcom.labingddd.lending.domain.InMemoryLoanRepository;
import com.anazcom.labingddd.lending.domain.InMemoryMemberDirectory;
import com.anazcom.labingddd.lending.domain.Loan;
import com.anazcom.labingddd.lending.domain.LoanId;
import com.anazcom.labingddd.lending.domain.LoanPeriod;
import com.anazcom.labingddd.lending.domain.MemberCannotBorrowException;
import com.anazcom.labingddd.member.domain.MemberId;
import com.anazcom.labingddd.shared.domain.DomainValidationException;

import java.time.LocalDate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BorrowBookUseCaseTest {

  private InMemoryBookCatalog books;
  private InMemoryMemberDirectory members;
  private InMemoryLoanRepository loans;
  private BorrowBookUseCase borrowBook;

  @BeforeEach
  void setUp() {
    books = new InMemoryBookCatalog();
    members = new InMemoryMemberDirectory();
    loans = new InMemoryLoanRepository();
    borrowBook = new BorrowBookUseCase(books, members, loans);
  }

  @Test
  void throwOnInvalidBookId() {
    final BookId bookId = BookId.generate();
    final MemberId memberId = MemberId.generate();

    Assertions.assertThatThrownBy(() -> borrowBook.handle(bookId, memberId))
        .isInstanceOf(DomainValidationException.class);
  }

  @Test
  void throwOnInvalidMemberId() {
    final BookId bookId = BookId.generate();
    books.add(bookId);
    final MemberId memberId = MemberId.generate();

    Assertions.assertThatThrownBy(() -> borrowBook.handle(bookId, memberId))
        .isInstanceOf(DomainValidationException.class);
  }

  @Test
  void throwWhenNotAbleToBorrow() {
    final BookId bookId = BookId.generate();
    books.add(bookId);
    final MemberId memberId = MemberId.generate();
    members.add(memberId, false);

    Assertions.assertThatThrownBy(() -> borrowBook.handle(bookId, memberId))
        .isInstanceOf(MemberCannotBorrowException.class);
  }

  @Test
  void throwWhenBookHasActiveLoan() {
    final BookId bookId = BookId.generate();
    books.add(bookId);
    final MemberId memberId = MemberId.generate();
    members.add(memberId, true);

    final Loan loan = new Loan(
        LoanId.generate(),
        bookId,
        memberId,
        new LoanPeriod(LocalDate.now(), LocalDate.now().plusWeeks(2)));
    loans.save(loan);

    Assertions.assertThatThrownBy(() -> borrowBook.handle(bookId, memberId))
        .isInstanceOf(BookAlreadyOnLoanException.class);
  }

  @Test
  void saveValidLoan() {
    final BookId bookId = BookId.generate();
    books.add(bookId);
    final MemberId memberId = MemberId.generate();
    members.add(memberId, true);

    final LoanId loanId = borrowBook.handle(bookId, memberId);
    Assertions.assertThat(loanId).isNotNull();
  }
}
