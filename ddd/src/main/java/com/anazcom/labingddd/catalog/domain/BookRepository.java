package com.anazcom.labingddd.catalog.domain;

import java.util.Optional;

public interface BookRepository {
  Optional<Book> findById(BookId id);

  void save(Book book);
}
