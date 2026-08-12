package com.anazcom.labingddd.lending.infrastructure.adapters;

import org.springframework.stereotype.Component;

import com.anazcom.labingddd.catalog.domain.BookId;
import com.anazcom.labingddd.catalog.domain.BookRepository;
import com.anazcom.labingddd.lending.domain.BookCatalog;

@Component
class BookCatalogAdapter implements BookCatalog {

  private final BookRepository books;

  BookCatalogAdapter(BookRepository books) {
    this.books = books;
  }

  @Override
  public boolean exists(BookId bookId) {
    return books.findById(bookId).isPresent();
  }

}
