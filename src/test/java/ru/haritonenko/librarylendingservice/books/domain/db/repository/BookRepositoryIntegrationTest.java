package ru.haritonenko.librarylendingservice.books.domain.db.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.haritonenko.librarylendingservice.books.domain.db.entity.BookEntity;
import ru.haritonenko.librarylendingservice.db.AbstractJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class BookRepositoryIntegrationTest extends AbstractJpaTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    void shouldSaveBookAndFindById() {
        BookEntity book = BookEntity.builder()
                .title("Repo Book")
                .author("Repo Author")
                .isbn("repo-isbn-1")
                .build();

        BookEntity savedBook = bookRepository.save(book);

        Optional<BookEntity> foundBookOpt = bookRepository.findById(savedBook.getId());

        assertTrue(foundBookOpt.isPresent());
        assertEquals("Repo Book", foundBookOpt.get().getTitle());
        assertEquals("Repo Author", foundBookOpt.get().getAuthor());
    }

    @Test
    void shouldReturnEmptyOptionalWhenBookNotFoundById() {
        Optional<BookEntity> foundBookOpt = bookRepository.findById(Long.MAX_VALUE);

        assertFalse(foundBookOpt.isPresent());
    }

    @Test
    void shouldFindBookByIsbn() {
        bookRepository.save(createBook("Repo ISBN Book", "Repo Author", "repo-isbn-find"));

        Optional<BookEntity> foundBookOpt = bookRepository.findByIsbn("repo-isbn-find");

        assertTrue(foundBookOpt.isPresent());
        assertEquals("Repo ISBN Book", foundBookOpt.get().getTitle());
    }

    @Test
    void shouldReturnTrueWhenBookExistsByIsbn() {
        bookRepository.save(createBook("Repo Exists Book", "Repo Author", "repo-isbn-exists"));

        boolean exists = bookRepository.existsByIsbn("repo-isbn-exists");

        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenBookDoesNotExistByIsbn() {
        boolean exists = bookRepository.existsByIsbn("missing-isbn");

        assertFalse(exists);
    }

    @Test
    void shouldFindBooksByTitleAndAuthorContainingIgnoreCase() {
        bookRepository.save(createBook("Search Java", "Search Author", "repo-isbn-search-one"));
        bookRepository.save(createBook("Search Java Advanced", "Another Search Author", "repo-isbn-search-two"));
        bookRepository.save(createBook("Different Book", "Other Author", "repo-isbn-search-missing"));

        org.springframework.data.domain.Page<BookEntity> foundBooks =
                bookRepository.findByTitleContainingIgnoreCaseAndAuthorContainingIgnoreCase(
                        "java",
                        "search",
                        org.springframework.data.domain.PageRequest.of(0, 10)
                );

        assertEquals(2, foundBooks.getTotalElements());
    }

    private BookEntity createBook(String title, String author, String isbn) {
        return BookEntity.builder()
                .title(title)
                .author(author)
                .isbn(isbn)
                .build();
    }
}
