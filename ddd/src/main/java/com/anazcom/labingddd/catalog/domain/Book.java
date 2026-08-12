package com.anazcom.labingddd.catalog.domain;

import com.anazcom.labingddd.shared.domain.DomainValidationException;
import com.anazcom.labingddd.shared.domain.Ids;

public class Book {
  private final BookId id;
  private final Isbn isbn;
  private String title;
  private String author;

  public Book(BookId id, Isbn isbn) {
    this.id = id;
    this.isbn = isbn;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Book other)) return false;
    return this.id.equals(other.id);
  }

  @Override
  public int hashCode() {
    // Important to setup hascode otherwise
    // can silently failed when storing books
    // on maps or sets
    return id.hashCode();
  }

  public BookId getId() {
    return id;
  }

  public Isbn getIsbn() {
    return isbn;
  }

  public String getTitle() {
    return title;
  }

  public void changeTitle(String newTitle) {
    Ids.requireNonBlank(newTitle, "Title");
    if (newTitle.length() > BookConstrains.TITLE_MAX_LENGTH) {
      throw new DomainValidationException("Title is too long");
    }
    this.title = newTitle;
  }

  public String getAuthor() {
    return author;
  }

  public void changeAuthor(String newAuthor) {
    Ids.requireNonBlank(newAuthor, "Author");
    if (newAuthor.length() > BookConstrains.AUTHOR_MAX_LENGTH) {
      throw new DomainValidationException("Author is too long");
    }
    this.author = newAuthor;
  }
}
