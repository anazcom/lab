package com.anazcom.labingddd.lending.domain;

import java.util.HashSet;
import java.util.Set;

import com.anazcom.labingddd.catalog.domain.BookId;

public class InMemoryBookCatalog implements BookCatalog {
  private final Set<BookId> books = new HashSet<>();

  @Override
  public boolean exists(BookId bookId) {
    return this.books.contains(bookId);
  }

  public void add(BookId bookId) {
    this.books.add(bookId);
  }
}
