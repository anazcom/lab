package com.anazcom.labingddd.lending.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anazcom.labingddd.catalog.domain.BookId;
import com.anazcom.labingddd.lending.application.BorrowBookUseCase;
import com.anazcom.labingddd.lending.domain.LoanId;
import com.anazcom.labingddd.lending.domain.LoanNotFoundException;
import com.anazcom.labingddd.lending.domain.LoanRepository;
import com.anazcom.labingddd.member.domain.MemberId;

@RestController
@RequestMapping("/loans")
class LoanController {

    private final BorrowBookUseCase borrowBookUseCase;
    private final LoanRepository loans;

    LoanController(BorrowBookUseCase borrowBookUseCase, LoanRepository loans) {
        this.borrowBookUseCase = borrowBookUseCase;
        this.loans = loans;
    }

    record BorrowBookRequest(String bookId, String memberId) {
    }

    record BorrowBookResponse(String id) {
    }

    @PostMapping
    ResponseEntity<BorrowBookResponse> borrowBook(@RequestBody BorrowBookRequest request) {
        BookId bookId = new BookId(request.bookId);
        MemberId memberId = new MemberId(request.memberId);

        LoanId loanId = borrowBookUseCase.handle(bookId, memberId);
        return ResponseEntity.ok(new BorrowBookResponse(loanId.value()));
    }

    record GetLoanResponse(String id, String bookId, String memberId, String startDate, String dueDate) {
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetLoanResponse> getLoan(@PathVariable("id") String id) {
        LoanId loanId = new LoanId(id);
        return loans.findById(loanId)
                .map(loan -> ResponseEntity.ok(new GetLoanResponse(
                        loan.getId().value(),
                        loan.getBookId().value(),
                        loan.getMemberId().value(),
                        loan.getPeriod().startDate().toString(),
                        loan.getPeriod().dueDate().toString())))
                .orElseThrow(() -> new LoanNotFoundException(loanId));
    }

}
