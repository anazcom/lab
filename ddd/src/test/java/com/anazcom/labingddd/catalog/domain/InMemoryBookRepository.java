package com.anazcom.labingddd.catalog.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryBookRepository implements BookRepository {
  private final Map<BookId, Book> books = new HashMap<>();

  @Override
  public Optional<Book> findById(BookId id) {
    return Optional.ofNullable(this.books.get(id));
  }

  @Override
  public void save(Book book) {
    this.books.put(book.getId(), book);
  }

  public void clear() {
    this.books.clear();
  }
}
