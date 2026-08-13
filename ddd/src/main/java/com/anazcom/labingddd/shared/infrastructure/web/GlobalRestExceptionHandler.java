package com.anazcom.labingddd.shared.infrastructure.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.anazcom.labingddd.catalog.domain.BookNotFoundException;
import com.anazcom.labingddd.lending.domain.BookAlreadyOnLoanException;
import com.anazcom.labingddd.lending.domain.LoanNotFoundException;
import com.anazcom.labingddd.lending.domain.MemberCannotBorrowException;
import com.anazcom.labingddd.member.domain.MemberNotFoundException;
import com.anazcom.labingddd.shared.domain.DomainRepositoryException;
import com.anazcom.labingddd.shared.domain.DomainStateException;
import com.anazcom.labingddd.shared.domain.DomainValidationException;

@RestControllerAdvice
class GlobalRestExceptionHandler {

  @ExceptionHandler({ BookNotFoundException.class, MemberNotFoundException.class, LoanNotFoundException.class })
  ProblemDetail handleNotFound(RuntimeException exc) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exc.getMessage());
  }

  @ExceptionHandler(DomainRepositoryException.class)
  ProblemDetail handleRepositoryException(DomainRepositoryException exc) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, exc.getMessage());
  }

  @ExceptionHandler(Exception.class)
  ProblemDetail handleUnexpectedException(Exception exc) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, exc.getMessage());
  }

  @ExceptionHandler({ BookAlreadyOnLoanException.class, MemberCannotBorrowException.class })
  ProblemDetail handleConflict(DomainStateException exc) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exc.getMessage());
  }

  @ExceptionHandler({ DomainValidationException.class, DomainStateException.class })
  ProblemDetail handleDomainValidation(RuntimeException exc) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exc.getMessage());
  }

}
