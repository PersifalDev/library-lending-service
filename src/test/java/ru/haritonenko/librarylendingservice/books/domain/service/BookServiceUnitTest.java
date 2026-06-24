package ru.haritonenko.librarylendingservice.books.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.core.RedisTemplate;
import ru.haritonenko.librarylendingservice.books.api.dto.BookCreateRequestDto;
import ru.haritonenko.librarylendingservice.books.api.dto.BookUpdateRequestDto;
import ru.haritonenko.librarylendingservice.books.domain.Book;
import ru.haritonenko.librarylendingservice.books.domain.db.entity.BookEntity;
import ru.haritonenko.librarylendingservice.books.domain.db.repository.BookRepository;
import ru.haritonenko.librarylendingservice.books.domain.exception.BookAlreadyExistsException;
import ru.haritonenko.librarylendingservice.books.domain.exception.BookNotFoundException;
import ru.haritonenko.librarylendingservice.books.domain.exception.IllegalBookArgumentException;
import ru.haritonenko.librarylendingservice.books.domain.mapper.BookMapper;
import ru.haritonenko.librarylendingservice.config.PageConfig;
import ru.haritonenko.librarylendingservice.config.properties.CacheProperties;
import ru.haritonenko.librarylendingservice.config.properties.SearchProperties;
import ru.haritonenko.librarylendingservice.lendings.domain.service.LendingCacheService;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceUnitTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookMapper bookMapper;
    @Mock
    private PageConfig pageConfig;
    @Mock
    private ObjectProvider<RedisTemplate<String, Book>> redisBookTemplateProvider;
    @Mock
    private CacheProperties cacheProperties;
    @Mock
    private SearchProperties searchProperties;
    @Mock
    private LendingCacheService lendingCacheService;

    private BookService bookService;
    private BookEntity bookEntity;
    private Book bookDomain;

    @BeforeEach
    void setUp() {
        when(redisBookTemplateProvider.getIfAvailable()).thenReturn(null);
        bookService = new BookService(
                bookRepository,
                bookMapper,
                pageConfig,
                redisBookTemplateProvider,
                cacheProperties,
                searchProperties,
                lendingCacheService
        );
        bookEntity = BookEntity.builder().id(1L).title("Book").author("Author").isbn("isbn").build();
        bookDomain = Book.builder().id(1L).title("Book").author("Author").isbn("isbn").build();
    }

    @Test
    void shouldSuccessfullyCreateBook() {
        BookCreateRequestDto requestDto = new BookCreateRequestDto();
        requestDto.setTitle("Book");
        requestDto.setAuthor("Author");
        requestDto.setIsbn("isbn");

        when(bookRepository.existsByIsbn("isbn")).thenReturn(false);
        when(bookRepository.save(any(BookEntity.class))).thenReturn(bookEntity);
        when(bookMapper.toDomain(bookEntity)).thenReturn(bookDomain);

        Book createdBook = bookService.createBook(requestDto);

        assertEquals(1L, createdBook.getId());
        assertEquals("Book", createdBook.getTitle());
        verify(bookRepository).save(any(BookEntity.class));
    }

    @Test
    void shouldSuccessfullyGetBookById() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(bookEntity));
        when(bookMapper.toDomain(bookEntity)).thenReturn(bookDomain);

        Book foundBook = bookService.getBookById(1L);

        assertEquals(bookDomain.getId(), foundBook.getId());
        verify(bookRepository).findById(1L);
    }

    @Test
    void shouldThrowIllegalBookArgumentExceptionWhenCreateRequestIsNull() {
        IllegalBookArgumentException exception = assertThrows(
                IllegalBookArgumentException.class,
                () -> bookService.createBook(null)
        );

        assertEquals("Book create request is null", exception.getMessage());
    }

    @Test
    void shouldThrowBookNotFoundExceptionWhenBookNotFoundById() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        BookNotFoundException exception = assertThrows(
                BookNotFoundException.class,
                () -> bookService.getBookById(999L)
        );

        assertEquals("Book with id=999 not found", exception.getMessage());
    }

    @Test
    void shouldSuccessfullyGetBooks() {
        when(searchProperties.getEmptyFilterValue()).thenReturn("");
        when(pageConfig.pageable(null, null)).thenReturn(org.springframework.data.domain.PageRequest.of(0, 10));
        when(bookRepository.findByTitleContainingIgnoreCaseAndAuthorContainingIgnoreCase(eq(""), eq(""), any()))
                .thenReturn(new PageImpl<BookEntity>(Collections.singletonList(bookEntity)));
        when(bookMapper.toDomain(bookEntity)).thenReturn(bookDomain);

        assertEquals(1, bookService.getBooks(null, null, null, null).getTotalElements());
    }

    @Test
    void shouldSuccessfullyUpdateBook() {
        BookUpdateRequestDto requestDto = new BookUpdateRequestDto();
        requestDto.setTitle("Updated Book");
        requestDto.setAuthor("Updated Author");
        requestDto.setIsbn("updated-isbn");
        BookEntity updatedEntity = BookEntity.builder()
                .id(1L)
                .title("Updated Book")
                .author("Updated Author")
                .isbn("updated-isbn")
                .build();
        Book updatedDomain = Book.builder()
                .id(1L)
                .title("Updated Book")
                .author("Updated Author")
                .isbn("updated-isbn")
                .build();

        when(bookRepository.findById(1L)).thenReturn(Optional.of(bookEntity));
        when(bookRepository.findByIsbn("updated-isbn")).thenReturn(Optional.empty());
        when(bookRepository.save(bookEntity)).thenReturn(updatedEntity);
        when(bookMapper.toDomain(updatedEntity)).thenReturn(updatedDomain);

        Book updatedBook = bookService.updateBook(1L, requestDto);

        assertEquals("Updated Book", updatedBook.getTitle());
        assertEquals("Updated Author", updatedBook.getAuthor());
        verify(lendingCacheService).invalidateByBookId(1L);
    }

    @Test
    void shouldThrowBookAlreadyExistsExceptionWhenBookWithIsbnAlreadyExistsOnCreate() {
        BookCreateRequestDto requestDto = new BookCreateRequestDto();
        requestDto.setTitle("Book");
        requestDto.setAuthor("Author");
        requestDto.setIsbn("isbn");
        when(bookRepository.existsByIsbn("isbn")).thenReturn(true);

        BookAlreadyExistsException exception = assertThrows(
                BookAlreadyExistsException.class,
                () -> bookService.createBook(requestDto)
        );

        assertEquals("Book with isbn=isbn already exists", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalBookArgumentExceptionWhenBookIdOrUpdateRequestIsNull() {
        BookUpdateRequestDto requestDto = new BookUpdateRequestDto();

        IllegalBookArgumentException exceptionForNullId = assertThrows(
                IllegalBookArgumentException.class,
                () -> bookService.updateBook(null, requestDto)
        );
        assertEquals("Book id or update request is null", exceptionForNullId.getMessage());

        IllegalBookArgumentException exceptionForNullRequest = assertThrows(
                IllegalBookArgumentException.class,
                () -> bookService.updateBook(1L, null)
        );
        assertEquals("Book id or update request is null", exceptionForNullRequest.getMessage());
    }
}
