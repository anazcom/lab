package com.anazcom.labingddd.catalog.infrastructure;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anazcom.labingddd.catalog.domain.Book;
import com.anazcom.labingddd.catalog.domain.BookId;
import com.anazcom.labingddd.catalog.domain.BookNotFoundException;
import com.anazcom.labingddd.catalog.domain.BookRepository;

@RestController
@RequestMapping("/books")
class BookController {

  record BookResponse(String id, String author, String title, String isbn) {

    static BookResponse from(Book book) {
      return new BookResponse(
          book.getId().value(), book.getAuthor(), book.getTitle(), book.getIsbn().value());
    }
  }

  private final BookRepository books;

  public BookController(BookRepository books) {
    this.books = books;
  }

  @GetMapping("/{id}")
  ResponseEntity<BookResponse> getBookBy(@PathVariable(name = "id") String id) {
    BookId bookId = new BookId(id);
    Book book = this.books.findById(bookId).orElseThrow(() -> new BookNotFoundException(bookId));
    return ResponseEntity.ok(BookResponse.from(book));
  }
}
