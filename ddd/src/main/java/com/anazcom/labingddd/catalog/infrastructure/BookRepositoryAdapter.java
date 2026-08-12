package com.anazcom.labingddd.catalog.infrastructure;

import com.anazcom.labingddd.catalog.domain.Book;
import com.anazcom.labingddd.catalog.domain.BookId;
import com.anazcom.labingddd.catalog.domain.BookRepository;
import com.anazcom.labingddd.catalog.domain.Isbn;
import com.anazcom.labingddd.shared.domain.DomainRepositoryException;

import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
class BookRepositoryAdapter implements BookRepository {

  private final SpringDataBookRepository books;

  BookRepositoryAdapter(SpringDataBookRepository books) {
    this.books = books;
  }

  @Override
  public Optional<Book> findById(BookId id) {
    final Optional<BookJpaEntity> optJpaBook = this.books.findById(id.value());

    if (optJpaBook.isEmpty()) {
      return Optional.empty();
    }

    final BookJpaEntity jpaBook = optJpaBook.get();

    final BookId bookId = new BookId(jpaBook.getId());
    final Isbn isbn = new Isbn(jpaBook.getIsbn());
    final Book book = new Book(bookId, isbn);
    book.changeTitle(jpaBook.getTitle());
    book.changeAuthor(jpaBook.getAuthor());

    return Optional.of(book);
  }

  @Override
  public void save(Book book) {

    final BookJpaEntity entity = new BookJpaEntity();
    entity.setId(book.getId().value());
    entity.setIsbn(book.getIsbn().value());
    entity.setTitle(book.getTitle());
    entity.setAuthor(book.getAuthor());

    try {
      this.books.save(entity);
    } catch (DataIntegrityViolationException exc) {
      throw new DomainRepositoryException("Failed to save book", exc);
    }
  }
}
