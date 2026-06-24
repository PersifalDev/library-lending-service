package ru.haritonenko.librarylendingservice.books.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.haritonenko.librarylendingservice.books.api.dto.BookCreateRequestDto;
import ru.haritonenko.librarylendingservice.books.api.dto.BookResponseDto;
import ru.haritonenko.librarylendingservice.books.api.dto.BookUpdateRequestDto;
import ru.haritonenko.librarylendingservice.books.domain.Book;
import ru.haritonenko.librarylendingservice.books.domain.exception.BookAlreadyExistsException;
import ru.haritonenko.librarylendingservice.books.domain.exception.BookNotFoundException;
import ru.haritonenko.librarylendingservice.books.domain.mapper.BookMapper;
import ru.haritonenko.librarylendingservice.books.domain.service.BookService;
import ru.haritonenko.librarylendingservice.clients.security.jwt.manager.JwtTokenManager;
import ru.haritonenko.librarylendingservice.config.properties.RateLimitProperties;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@AutoConfigureMockMvc(addFilters = false)
public class BookControllerWebOnlyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @MockBean
    private BookMapper bookMapper;

    @MockBean
    private JwtTokenManager jwtTokenManager;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @MockBean
    private RateLimitProperties rateLimitProperties;

    @BeforeEach
    void setUp() {
        when(rateLimitProperties.isEnabled()).thenReturn(false);
    }

    @Test
    void shouldSuccessfullyGetBooks() throws Exception {
        Book book = createBook(1L, "Book", "Author", "isbn");
        BookResponseDto responseDto = createResponseDto(1L, "Book", "Author", "isbn");

        when(bookService.getBooks(eq("Bo"), eq("Au"), eq(0), eq(10)))
                .thenReturn(new PageImpl<Book>(Collections.singletonList(book)));
        when(bookMapper.toResponseDto(book)).thenReturn(responseDto);

        mockMvc.perform(get("/api/books/search")
                        .param("title", "Bo")
                        .param("author", "Au")
                        .param("pageNumber", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("Book"));
    }

    @Test
    void shouldSuccessfullyCreateBook() throws Exception {
        BookCreateRequestDto requestDto = createBookRequest("Book", "Author", "isbn");
        Book book = createBook(1L, "Book", "Author", "isbn");
        BookResponseDto responseDto = createResponseDto(1L, "Book", "Author", "isbn");

        when(bookService.createBook(any(BookCreateRequestDto.class))).thenReturn(book);
        when(bookMapper.toResponseDto(book)).thenReturn(responseDto);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Book"));
    }

    @Test
    void shouldSuccessfullyGetBookById() throws Exception {
        Book book = createBook(1L, "Book", "Author", "isbn");
        BookResponseDto responseDto = createResponseDto(1L, "Book", "Author", "isbn");

        when(bookService.getBookById(1L)).thenReturn(book);
        when(bookMapper.toResponseDto(book)).thenReturn(responseDto);

        mockMvc.perform(get("/api/books/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void shouldSuccessfullyUpdateBook() throws Exception {
        BookUpdateRequestDto requestDto = new BookUpdateRequestDto();
        requestDto.setTitle("Updated Book");
        requestDto.setAuthor("Updated Author");
        requestDto.setIsbn("updated-isbn");
        Book book = createBook(1L, "Updated Book", "Updated Author", "updated-isbn");
        BookResponseDto responseDto = createResponseDto(1L, "Updated Book", "Updated Author", "updated-isbn");

        when(bookService.updateBook(eq(1L), any(BookUpdateRequestDto.class))).thenReturn(book);
        when(bookMapper.toResponseDto(book)).thenReturn(responseDto);

        mockMvc.perform(put("/api/books/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Book"));
    }

    @Test
    void shouldReturnBadRequestWhenCreateBookRequestIsInvalid() throws Exception {
        BookCreateRequestDto requestDto = createBookRequest("", "", "");

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenUpdateBookRequestIsInvalid() throws Exception {
        BookUpdateRequestDto requestDto = new BookUpdateRequestDto();
        requestDto.setTitle("");
        requestDto.setAuthor("");
        requestDto.setIsbn("");

        mockMvc.perform(put("/api/books/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenBookIdHasInvalidType() throws Exception {
        mockMvc.perform(get("/api/books/{id}", "not-a-number"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenBookPageParametersAreInvalid() throws Exception {
        mockMvc.perform(get("/api/books/search")
                        .param("pageNumber", "-1")
                        .param("pageSize", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation error"));

        mockMvc.perform(get("/api/books/search")
                        .param("pageNumber", "0")
                        .param("pageSize", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation error"));

        mockMvc.perform(get("/api/books/search")
                        .param("pageNumber", "0")
                        .param("pageSize", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation error"));
    }

    @Test
    void shouldReturnBadRequestWhenBookIdIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/books/{id}", 0L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation error"));

        BookUpdateRequestDto requestDto = new BookUpdateRequestDto();
        requestDto.setTitle("Updated Book");
        requestDto.setAuthor("Updated Author");
        requestDto.setIsbn("updated-isbn");

        mockMvc.perform(put("/api/books/{id}", -1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation error"));
    }

    @Test
    void shouldReturnNotFoundWhenBookDoesNotExist() throws Exception {
        when(bookService.getBookById(404L))
                .thenThrow(new BookNotFoundException("Book with id=404 not found"));

        mockMvc.perform(get("/api/books/{id}", 404L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found"));
    }

    @Test
    void shouldReturnConflictWhenBookWithIsbnAlreadyExistsOnCreate() throws Exception {
        BookCreateRequestDto requestDto = createBookRequest("Book", "Author", "isbn");

        when(bookService.createBook(any(BookCreateRequestDto.class)))
                .thenThrow(new BookAlreadyExistsException("Book with isbn=isbn already exists"));

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Book already exists"));
    }

    @Test
    void shouldReturnConflictWhenBookWithIsbnAlreadyExistsOnUpdate() throws Exception {
        BookUpdateRequestDto requestDto = new BookUpdateRequestDto();
        requestDto.setTitle("Updated Book");
        requestDto.setAuthor("Updated Author");
        requestDto.setIsbn("isbn");

        when(bookService.updateBook(eq(1L), any(BookUpdateRequestDto.class)))
                .thenThrow(new BookAlreadyExistsException("Book with isbn=isbn already exists"));

        mockMvc.perform(put("/api/books/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Book already exists"));
    }

    private BookCreateRequestDto createBookRequest(String title, String author, String isbn) {
        BookCreateRequestDto requestDto = new BookCreateRequestDto();
        requestDto.setTitle(title);
        requestDto.setAuthor(author);
        requestDto.setIsbn(isbn);
        return requestDto;
    }

    private Book createBook(Long id, String title, String author, String isbn) {
        return Book.builder()
                .id(id)
                .title(title)
                .author(author)
                .isbn(isbn)
                .build();
    }

    private BookResponseDto createResponseDto(Long id, String title, String author, String isbn) {
        BookResponseDto responseDto = new BookResponseDto();
        responseDto.setId(id);
        responseDto.setTitle(title);
        responseDto.setAuthor(author);
        responseDto.setIsbn(isbn);
        return responseDto;
    }
}
