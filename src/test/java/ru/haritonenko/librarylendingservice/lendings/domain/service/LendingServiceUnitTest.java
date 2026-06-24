package ru.haritonenko.librarylendingservice.lendings.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.haritonenko.librarylendingservice.books.domain.db.entity.BookEntity;
import ru.haritonenko.librarylendingservice.books.domain.service.BookService;
import ru.haritonenko.librarylendingservice.clients.domain.db.entity.ClientEntity;
import ru.haritonenko.librarylendingservice.clients.domain.service.ClientService;
import ru.haritonenko.librarylendingservice.config.PageConfig;
import ru.haritonenko.librarylendingservice.lendings.api.dto.LendingCreateRequestDto;
import ru.haritonenko.librarylendingservice.lendings.domain.Lending;
import ru.haritonenko.librarylendingservice.lendings.domain.db.entity.LendingEntity;
import ru.haritonenko.librarylendingservice.lendings.domain.db.repository.LendingRepository;
import ru.haritonenko.librarylendingservice.lendings.domain.exception.IllegalLendingArgumentException;
import ru.haritonenko.librarylendingservice.lendings.domain.exception.IllegalLendingStateException;
import ru.haritonenko.librarylendingservice.lendings.domain.exception.LendingNotFoundException;
import ru.haritonenko.librarylendingservice.lendings.domain.mapper.LendingMapper;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LendingServiceUnitTest {

    @Mock
    private LendingRepository lendingRepository;
    @Mock
    private BookService bookService;
    @Mock
    private ClientService clientService;
    @Mock
    private LendingMapper lendingMapper;
    @Mock
    private PageConfig pageConfig;
    @Mock
    private LendingCacheService lendingCacheService;

    private LendingService lendingService;
    private LendingEntity lendingEntity;
    private Lending lendingDomain;

    @BeforeEach
    void setUp() {
        lendingService = new LendingService(
                lendingRepository,
                bookService,
                clientService,
                lendingMapper,
                pageConfig,
                lendingCacheService
        );
        lendingEntity = LendingEntity.builder()
                .id(1L)
                .client(ClientEntity.builder().id(1L).build())
                .book(BookEntity.builder().id(1L).build())
                .takenAt(LocalDateTime.parse("2026-01-01T10:00:00"))
                .build();
        lendingDomain = Lending.builder()
                .id(1L)
                .takenAt(LocalDateTime.parse("2026-01-01T10:00:00"))
                .build();
    }

    @Test
    void shouldSuccessfullyCreateLending() {
        LendingCreateRequestDto requestDto = new LendingCreateRequestDto();
        requestDto.setClientId(1L);
        requestDto.setBookId(1L);
        requestDto.setTakenAt(LocalDateTime.parse("2026-01-01T10:00:00"));

        when(clientService.getClientEntityById(1L)).thenReturn(ClientEntity.builder().id(1L).build());
        when(bookService.getBookEntityById(1L)).thenReturn(BookEntity.builder().id(1L).build());
        when(lendingRepository.save(any(LendingEntity.class))).thenReturn(lendingEntity);
        when(lendingMapper.toDomain(lendingEntity)).thenReturn(lendingDomain);

        Lending createdLending = lendingService.createLending(requestDto);

        assertEquals(1L, createdLending.getId());
        verify(lendingRepository).save(any(LendingEntity.class));
        verify(lendingCacheService).cacheLending(lendingDomain);
    }

    @Test
    void shouldSuccessfullyGetLendingById() {
        when(lendingCacheService.getLending(1L)).thenReturn(null);
        when(lendingRepository.findWithClientAndBookById(1L)).thenReturn(Optional.of(lendingEntity));
        when(lendingMapper.toDomain(lendingEntity)).thenReturn(lendingDomain);

        Lending foundLending = lendingService.getLendingById(1L);

        assertEquals(1L, foundLending.getId());
        verify(lendingCacheService).cacheLending(lendingDomain);
    }

    @Test
    void shouldSuccessfullyReturnLending() {
        when(lendingRepository.findWithClientAndBookById(1L)).thenReturn(Optional.of(lendingEntity));
        when(lendingRepository.save(lendingEntity)).thenReturn(lendingEntity);
        when(lendingMapper.toDomain(lendingEntity)).thenReturn(lendingDomain);

        Lending returnedLending = lendingService.returnLending(1L);

        assertEquals(1L, returnedLending.getId());
        assertNotNull(lendingEntity.getReturnedAt());
        verify(lendingCacheService).invalidateLending(1L);
        verify(lendingCacheService).cacheLending(lendingDomain);
    }

    @Test
    void shouldThrowIllegalLendingArgumentExceptionWhenLendingCreateRequestIsNull() {
        IllegalLendingArgumentException exception = assertThrows(
                IllegalLendingArgumentException.class,
                () -> lendingService.createLending(null)
        );

        assertEquals("Lending create request is null", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalLendingArgumentExceptionWhenLendingIdIsNull() {
        IllegalLendingArgumentException exception = assertThrows(
                IllegalLendingArgumentException.class,
                () -> lendingService.getLendingById(null)
        );

        assertEquals("Lending id is null", exception.getMessage());
    }

    @Test
    void shouldThrowLendingNotFoundExceptionWhenLendingNotFoundById() {
        when(lendingCacheService.getLending(999L)).thenReturn(null);
        when(lendingRepository.findWithClientAndBookById(999L)).thenReturn(Optional.empty());

        LendingNotFoundException exception = assertThrows(
                LendingNotFoundException.class,
                () -> lendingService.getLendingById(999L)
        );

        assertEquals("Lending with id=999 not found", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalLendingStateExceptionWhenLendingAlreadyReturned() {
        lendingEntity.setReturnedAt(LocalDateTime.parse("2026-01-02T10:00:00"));
        when(lendingRepository.findWithClientAndBookById(1L)).thenReturn(Optional.of(lendingEntity));

        IllegalLendingStateException exception = assertThrows(
                IllegalLendingStateException.class,
                () -> lendingService.returnLending(1L)
        );

        assertEquals("Lending with id=1 already returned", exception.getMessage());
    }
}
