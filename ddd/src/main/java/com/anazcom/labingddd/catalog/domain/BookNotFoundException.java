package com.anazcom.labingddd.catalog.domain;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(BookId id) {
        super("Book not found: " + id.value());
    }
}
