package com.anazcom.labingddd.lending.domain;

import com.anazcom.labingddd.catalog.domain.BookId;
import com.anazcom.labingddd.shared.domain.DomainStateException;

public class BookAlreadyOnLoanException extends DomainStateException {

    public BookAlreadyOnLoanException(BookId bookId) {
        super("Book is already on loan: " + bookId.value());
    }

}
