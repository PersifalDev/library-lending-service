package ru.haritonenko.librarylendingservice.books.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import java.util.Objects;

@Slf4j
@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final PageConfig pageConfig;
    private final RedisTemplate<String, Book> redisBookTemplate;
    private final CacheProperties cacheProperties;
    private final SearchProperties searchProperties;
    private final LendingCacheService lendingCacheService;

    public BookService(
            BookRepository bookRepository,
            BookMapper bookMapper,
            PageConfig pageConfig,
            ObjectProvider<RedisTemplate<String, Book>> redisBookTemplateProvider,
            CacheProperties cacheProperties,
            SearchProperties searchProperties,
            LendingCacheService lendingCacheService
    ) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
        this.pageConfig = pageConfig;
        this.redisBookTemplate = redisBookTemplateProvider.getIfAvailable();
        this.cacheProperties = cacheProperties;
        this.searchProperties = searchProperties;
        this.lendingCacheService = lendingCacheService;
    }

    @Transactional
    public Book createBook(BookCreateRequestDto requestDto) {
        if (requestDto == null) {
            log.warn("Book create request is null");
            throw new IllegalBookArgumentException("Book create request is null");
        }
        if (bookRepository.existsByIsbn(requestDto.getIsbn())) {
            log.warn("Book with isbn={} already exists", requestDto.getIsbn());
            throw new BookAlreadyExistsException(
                    String.format("Book with isbn=%s already exists", requestDto.getIsbn())
            );
        }

        BookEntity bookEntity = BookEntity.builder()
                .title(requestDto.getTitle())
                .author(requestDto.getAuthor())
                .isbn(requestDto.getIsbn())
                .build();

        Book book = mapToDomain(bookRepository.save(bookEntity));
        cacheBook(book);
        log.info("Book created with id={}", book.getId());
        return book;
    }

    @Transactional(readOnly = true)
    public Book getBookById(Long id) {
        if (id == null) {
            log.warn("Book id is null");
            throw new IllegalBookArgumentException("Book id is null");
        }

        String key = getCacheKey(id);
        Book cachedBook = getBookFromCache(key);
        if (cachedBook != null) {
            return cachedBook;
        }

        Book book = mapToDomain(findBookById(id));
        cacheBook(book);
        return book;
    }

    @Transactional(readOnly = true)
    public Page<Book> getBooks(String title, String author, Integer pageNumber, Integer pageSize) {
        String normalizedTitle = title == null ? searchProperties.getEmptyFilterValue() : title;
        String normalizedAuthor = author == null ? searchProperties.getEmptyFilterValue() : author;
        return bookRepository
                .findByTitleContainingIgnoreCaseAndAuthorContainingIgnoreCase(
                        normalizedTitle,
                        normalizedAuthor,
                        pageConfig.pageable(pageNumber, pageSize)
                )
                .map(bookMapper::toDomain);
    }

    @Transactional
    public Book updateBook(Long id, BookUpdateRequestDto requestDto) {
        if (id == null || requestDto == null) {
            log.warn("Book id or update request is null");
            throw new IllegalBookArgumentException("Book id or update request is null");
        }

        BookEntity bookEntity = findBookById(id);
        bookRepository.findByIsbn(requestDto.getIsbn())
                .filter(existing -> !Objects.equals(existing.getId(), id))
                .ifPresent(existing -> {
                    log.warn("Book with isbn={} already exists", requestDto.getIsbn());
                    throw new BookAlreadyExistsException(
                            String.format("Book with isbn=%s already exists for another book", requestDto.getIsbn())
                    );
                });

        bookEntity.setTitle(requestDto.getTitle());
        bookEntity.setAuthor(requestDto.getAuthor());
        bookEntity.setIsbn(requestDto.getIsbn());

        Book book = mapToDomain(bookRepository.save(bookEntity));
        cacheBook(book);
        lendingCacheService.invalidateByBookId(book.getId());
        return book;
    }

    @Transactional(readOnly = true)
    public BookEntity getBookEntityById(Long id) {
        if (id == null) {
            log.warn("Book id is null");
            throw new IllegalBookArgumentException("Book id is null");
        }
        return findBookById(id);
    }

    private BookEntity findBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Book with id={} not found", id);
                    return new BookNotFoundException(String.format("Book with id=%d not found", id));
                });
    }

    private Book mapToDomain(BookEntity bookEntity) {
        return bookMapper.toDomain(bookEntity);
    }

    private Book getBookFromCache(String key) {
        if (redisBookTemplate == null) {
            return null;
        }
        try {
            return redisBookTemplate.opsForValue().get(key);
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis unavailable during book cache read, fallback to DB. key={}", key, ex);
            return null;
        }
    }

    private void cacheBook(Book book) {
        if (redisBookTemplate == null || book == null || book.getId() == null) {
            return;
        }
        String key = getCacheKey(book.getId());
        try {
            redisBookTemplate.opsForValue().set(key, book, cacheProperties.getBooksTtl());
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis unavailable during book cache write. key={}", key, ex);
        }
    }

    private String getCacheKey(Long id) {
        return cacheProperties.getBookKeyPrefix() + id;
    }
}
