package com.anazcom.labingddd.lending.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.anazcom.labingddd.catalog.domain.BookId;
import com.anazcom.labingddd.lending.application.BorrowBookUseCase;
import com.anazcom.labingddd.lending.domain.BookCatalog;
import com.anazcom.labingddd.lending.domain.InMemoryBookCatalog;
import com.anazcom.labingddd.lending.domain.InMemoryLoanRepository;
import com.anazcom.labingddd.lending.domain.InMemoryMemberDirectory;
import com.anazcom.labingddd.lending.domain.LoanRepository;
import com.anazcom.labingddd.lending.domain.MemberDirectory;
import com.anazcom.labingddd.member.domain.MemberId;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(LoanController.class)
class LoanControllerTest {
  @Autowired
  BookCatalog bookCatalog;
  @Autowired
  MemberDirectory memberDirectory;
  @Autowired
  LoanRepository loanRepository;
  @Autowired
  MockMvc mvc;
  @Autowired
  ObjectMapper objectMapper;

  @TestConfiguration
  static class TestConfig {
    @Bean
    BookCatalog bookCatalog() {
      return new InMemoryBookCatalog();
    }

    @Bean
    MemberDirectory memberDirectory() {
      return new InMemoryMemberDirectory();
    }

    @Bean
    LoanRepository loanRepository() {
      return new InMemoryLoanRepository();
    }

    @Bean
    BorrowBookUseCase borrowBookUseCase(BookCatalog bookCatalog, MemberDirectory memberDirectory,
        LoanRepository loanRepository) {
      return new BorrowBookUseCase(bookCatalog, memberDirectory, loanRepository);
    }
  }

  @BeforeEach
  void setup() {
    ((InMemoryBookCatalog) bookCatalog).clear();
    ((InMemoryMemberDirectory) memberDirectory).clear();
    ((InMemoryLoanRepository) loanRepository).clear();
  }

  @Test
  void shouldGetOk_whenValidIds() throws Exception {
    BookId bookId = BookId.generate();
    MemberId memberId = MemberId.generate();
    ((InMemoryBookCatalog) bookCatalog).add(bookId);
    ((InMemoryMemberDirectory) memberDirectory).add(memberId, true);

    LoanController.BorrowBookRequest request = new LoanController.BorrowBookRequest(bookId.value(), memberId.value());

    mvc.perform(
        post("/loans")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @Test
  void shouldGetBadRequest_whenBookNotFound() throws Exception {
    BookId bookId = BookId.generate();
    MemberId memberId = MemberId.generate();
    ((InMemoryMemberDirectory) memberDirectory).add(memberId, true);

    LoanController.BorrowBookRequest request = new LoanController.BorrowBookRequest(bookId.value(), memberId.value());

    mvc.perform(
        post("/loans")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldGetBadRequest_whenMemberNotFound() throws Exception {
    BookId bookId = BookId.generate();
    MemberId memberId = MemberId.generate();
    ((InMemoryBookCatalog) bookCatalog).add(bookId);

    LoanController.BorrowBookRequest request = new LoanController.BorrowBookRequest(bookId.value(), memberId.value());

    mvc.perform(
        post("/loans")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldGetConflict_whenMemberCannotBorrow() throws Exception {
    BookId bookId = BookId.generate();
    MemberId memberId = MemberId.generate();
    ((InMemoryBookCatalog) bookCatalog).add(bookId);
    ((InMemoryMemberDirectory) memberDirectory).add(memberId, false);

    LoanController.BorrowBookRequest request = new LoanController.BorrowBookRequest(bookId.value(), memberId.value());

    mvc.perform(
        post("/loans")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());
  }

  @Test
  void shouldGetConflict_whenBookAlreadyOnLoan() throws Exception {
    BookId bookId = BookId.generate();
    MemberId memberId1 = MemberId.generate();
    MemberId memberId2 = MemberId.generate();
    ((InMemoryBookCatalog) bookCatalog).add(bookId);
    ((InMemoryMemberDirectory) memberDirectory).add(memberId1, true);
    ((InMemoryMemberDirectory) memberDirectory).add(memberId2, true);

    // First loan
    LoanController.BorrowBookRequest request1 = new LoanController.BorrowBookRequest(bookId.value(), memberId1.value());
    mvc.perform(
        post("/loans")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request1)))
        .andExpect(status().isOk());

    // Second loan attempt for the same book
    LoanController.BorrowBookRequest request2 = new LoanController.BorrowBookRequest(bookId.value(), memberId2.value());
    mvc.perform(
        post("/loans")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request2)))
        .andExpect(status().isConflict());
  }
}
