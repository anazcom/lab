package com.anazcom.labingddd.lending.infrastructure;

import java.time.LocalDate;

import com.anazcom.labingddd.lending.domain.LoanStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Loans")
class LoanJpaEntity {
  @Id
  private String id;

  @Column(name = "book_id")
  private String bookId;

  @Column(name = "member_id")
  private String memberId;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private LoanStatus status;

  @Column(name = "start_date")
  private LocalDate startDate;

  @Column(name = "due_date")
  private LocalDate dueDate;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getBookId() {
    return bookId;
  }

  public void setBookId(String bookId) {
    this.bookId = bookId;
  }

  public String getMemberId() {
    return memberId;
  }

  public void setMemberId(String memberId) {
    this.memberId = memberId;
  }

  public LoanStatus getStatus() {
    return status;
  }

  public void setStatus(LoanStatus status) {
    this.status = status;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDate endDate) {
    this.dueDate = endDate;
  }
}
