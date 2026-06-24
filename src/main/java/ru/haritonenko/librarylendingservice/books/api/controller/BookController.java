package ru.haritonenko.librarylendingservice.books.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.haritonenko.librarylendingservice.books.api.dto.BookCreateRequestDto;
import ru.haritonenko.librarylendingservice.books.api.dto.BookResponseDto;
import ru.haritonenko.librarylendingservice.books.api.dto.BookUpdateRequestDto;
import ru.haritonenko.librarylendingservice.books.api.dto.filter.BookPageFilter;
import ru.haritonenko.librarylendingservice.books.domain.mapper.BookMapper;
import ru.haritonenko.librarylendingservice.books.domain.service.BookService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
@Tag(name = "Books", description = "Book catalog operations")
public class BookController {

    private final BookService bookService;
    private final BookMapper bookMapper;

    @Operation(summary = "Create book", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Book created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "409", description = "Book with ISBN already exists")
    })
    @PostMapping
    public ResponseEntity<BookResponseDto> createBook(@Valid @RequestBody BookCreateRequestDto requestDto) {
        log.info("POST /api/books");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bookMapper.toResponseDto(bookService.createBook(requestDto)));
    }

    @Operation(summary = "Get book by id", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book found"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDto> getBookById(
            @PathVariable("id") @Positive(message = "Book id must be positive") Long id
    ) {
        log.info("GET /api/books/{}", id);
        return ResponseEntity.ok(bookMapper.toResponseDto(bookService.getBookById(id)));
    }

    @Operation(summary = "Search books", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Books page returned"),
            @ApiResponse(responseCode = "400", description = "Invalid page parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<BookResponseDto>> getBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @Valid @ModelAttribute BookPageFilter pageFilter
    ) {
        log.info("GET /api/books/search");
        return ResponseEntity.ok(
                bookService.getBooks(title, author, pageFilter.getPageNumber(), pageFilter.getPageSize())
                        .map(bookMapper::toResponseDto)
        );
    }

    @Operation(summary = "Update book", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Book not found"),
            @ApiResponse(responseCode = "409", description = "Book with ISBN already exists")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BookResponseDto> updateBook(
            @PathVariable("id") @Positive(message = "Book id must be positive") Long id,
            @Valid @RequestBody BookUpdateRequestDto requestDto
    ) {
        log.info("PUT /api/books/{}", id);
        return ResponseEntity.ok(bookMapper.toResponseDto(bookService.updateBook(id, requestDto)));
    }
}
