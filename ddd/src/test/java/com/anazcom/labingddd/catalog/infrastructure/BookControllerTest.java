package com.anazcom.labingddd.catalog.infrastructure;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.anazcom.labingddd.catalog.domain.Book;
import com.anazcom.labingddd.catalog.domain.BookId;
import com.anazcom.labingddd.catalog.domain.BookRepository;
import com.anazcom.labingddd.catalog.domain.InMemoryBookRepository;
import com.anazcom.labingddd.catalog.domain.Isbn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookController.class)
class BookControllerTest {

  @Autowired private MockMvc mvc;
  @Autowired private BookRepository books;

  @TestConfiguration
  static class TestConfig {
    @Bean
    BookRepository bookRepository() {
      return new InMemoryBookRepository();
    }
  }

  @BeforeEach
  void setup() {
    ((InMemoryBookRepository) books).clear();
  }

  @Test
  void shouldGetOk_wheValidId() {
    BookId id = BookId.generate();
    Book book = new Book(id, new Isbn("9780134685991"));
    book.changeTitle("Effective Java");
    book.changeAuthor("Joshua Bloch");
    books.save(book);

    try {
      mvc.perform(get("/books/" + id.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.title").value("Effective Java"));
    } catch (Exception e) {
      throw new RuntimeException("failed performing mocks", e);
    }
  }

  @Test
  void shouldGetNotFound_whenIdNotFound() {
    BookId id = BookId.generate();

    try {
      mvc.perform(get("/books/" + id.value())).andExpect(status().isNotFound());

    } catch (Exception e) {
      throw new RuntimeException("failed performing mocks", e);
    }
  }
}
