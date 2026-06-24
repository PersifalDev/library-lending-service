package ru.haritonenko.librarylendingservice.books.domain.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.haritonenko.librarylendingservice.books.api.dto.BookCreateRequestDto;
import ru.haritonenko.librarylendingservice.books.api.dto.BookUpdateRequestDto;
import ru.haritonenko.librarylendingservice.books.domain.Book;
import ru.haritonenko.librarylendingservice.books.domain.db.entity.BookEntity;
import ru.haritonenko.librarylendingservice.books.domain.db.repository.BookRepository;
import ru.haritonenko.librarylendingservice.books.domain.exception.BookNotFoundException;
import ru.haritonenko.librarylendingservice.integration.AbstractIntegrationTest;

import static org.junit.jupiter.api.Assertions.*;

public class BookServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @Transactional
    @Test
    void shouldSuccessfullyCreateBook() {
        BookCreateRequestDto requestDto = new BookCreateRequestDto();
        requestDto.setTitle("integration-book");
        requestDto.setAuthor("integration-author");
        requestDto.setIsbn("integration-isbn");

        Book createdBook = bookService.createBook(requestDto);

        assertNotNull(createdBook.getId());
        assertEquals("integration-book", createdBook.getTitle());
        assertTrue(bookRepository.findById(createdBook.getId()).isPresent());
    }

    @Transactional
    @Test
    void shouldSuccessfullyGetBookById() {
        BookEntity savedBook = bookRepository.save(BookEntity.builder()
                .title("dummy-book")
                .author("dummy-author")
                .isbn("dummy-isbn")
                .build());

        Book foundBook = bookService.getBookById(savedBook.getId());

        assertEquals(savedBook.getId(), foundBook.getId());
        assertEquals("dummy-book", foundBook.getTitle());
    }

    @Transactional
    @Test
    void shouldThrowBookNotFoundExceptionWhenBookNotFoundById() {
        BookNotFoundException exception = assertThrows(
                BookNotFoundException.class,
                () -> bookService.getBookById(Long.MAX_VALUE)
        );

        assertEquals(String.format("Book with id=%d not found", Long.MAX_VALUE), exception.getMessage());
    }

    @Transactional
    @Test
    void shouldSuccessfullyUpdateBook() {
        BookEntity savedBook = bookRepository.save(BookEntity.builder()
                .title("book-before-update")
                .author("author-before-update")
                .isbn("isbn-before-update")
                .build());
        BookUpdateRequestDto requestDto = new BookUpdateRequestDto();
        requestDto.setTitle("book-after-update");
        requestDto.setAuthor("author-after-update");
        requestDto.setIsbn("isbn-after-update");

        Book updatedBook = bookService.updateBook(savedBook.getId(), requestDto);

        assertEquals(savedBook.getId(), updatedBook.getId());
        assertEquals("book-after-update", updatedBook.getTitle());
        assertEquals("author-after-update", updatedBook.getAuthor());
        assertEquals("isbn-after-update", updatedBook.getIsbn());
    }

    @Transactional
    @Test
    void shouldThrowIllegalBookArgumentExceptionWhenBookCreateRequestIsNull() {
        assertThrows(
                ru.haritonenko.librarylendingservice.books.domain.exception.IllegalBookArgumentException.class,
                () -> bookService.createBook(null)
        );
    }
}
