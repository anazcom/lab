package com.anazcom.labingddd.lending.infrastructure;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anazcom.labingddd.catalog.domain.BookId;
import com.anazcom.labingddd.lending.application.BorrowBookUseCase;
import com.anazcom.labingddd.lending.domain.LoanId;
import com.anazcom.labingddd.member.domain.MemberId;

@RestController
@RequestMapping("/loans")
class LoanController {

    private final BorrowBookUseCase borrowBookUseCase;

    LoanController(BorrowBookUseCase borrowBookUseCase) {
        this.borrowBookUseCase = borrowBookUseCase;
    }

    record BorrowBookRequest(String bookId, String memberId) {
    }

    record BorrowBookResponse(String id) {
    }

    @PostMapping
    public ResponseEntity<BorrowBookResponse> borrowBook(@RequestBody BorrowBookRequest request) {
        BookId bookId = new BookId(request.bookId);
        MemberId memberId = new MemberId(request.memberId);

        LoanId loanId = borrowBookUseCase.handle(bookId, memberId);
        return ResponseEntity.ok(new BorrowBookResponse(loanId.value()));
    }

}
