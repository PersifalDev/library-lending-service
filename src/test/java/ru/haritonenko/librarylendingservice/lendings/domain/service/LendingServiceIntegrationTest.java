package ru.haritonenko.librarylendingservice.lendings.domain.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import ru.haritonenko.librarylendingservice.books.domain.db.entity.BookEntity;
import ru.haritonenko.librarylendingservice.books.domain.db.repository.BookRepository;
import ru.haritonenko.librarylendingservice.clients.domain.db.entity.ClientEntity;
import ru.haritonenko.librarylendingservice.clients.domain.db.repository.ClientRepository;
import ru.haritonenko.librarylendingservice.clients.domain.role.ClientRole;
import ru.haritonenko.librarylendingservice.integration.AbstractIntegrationTest;
import ru.haritonenko.librarylendingservice.lendings.api.dto.LendingCreateRequestDto;
import ru.haritonenko.librarylendingservice.lendings.domain.Lending;
import ru.haritonenko.librarylendingservice.lendings.domain.db.entity.LendingEntity;
import ru.haritonenko.librarylendingservice.lendings.domain.db.repository.LendingRepository;
import ru.haritonenko.librarylendingservice.lendings.domain.exception.IllegalLendingArgumentException;
import ru.haritonenko.librarylendingservice.lendings.domain.exception.IllegalLendingStateException;
import ru.haritonenko.librarylendingservice.lendings.domain.exception.LendingNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class LendingServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private LendingService lendingService;

    @Autowired
    private LendingRepository lendingRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private BookRepository bookRepository;

    @Transactional
    @Test
    void shouldSuccessfullyCreateLending() {
        ClientEntity client = saveDummyClient("lending_service_create_client");
        BookEntity book = saveDummyBook("lending-service-create-isbn");
        LendingCreateRequestDto requestDto = createRequest(client.getId(), book.getId());

        Lending createdLending = lendingService.createLending(requestDto);

        assertNotNull(createdLending.getId());
        assertEquals(LocalDateTime.parse("2026-01-01T10:00:00"), createdLending.getTakenAt());
        assertTrue(lendingRepository.findById(createdLending.getId()).isPresent());
    }

    @Transactional
    @Test
    void shouldSuccessfullyGetLendingById() {
        LendingEntity savedLending = saveDummyLending("lending_service_get_client", "lending-service-get-isbn");

        Lending foundLending = lendingService.getLendingById(savedLending.getId());

        assertEquals(savedLending.getId(), foundLending.getId());
        assertEquals(savedLending.getTakenAt(), foundLending.getTakenAt());
    }

    @Transactional
    @Test
    void shouldSuccessfullyReturnLending() {
        LendingEntity savedLending = saveDummyLending("lending_service_return_client", "lending-service-return-isbn");

        Lending returnedLending = lendingService.returnLending(savedLending.getId());

        assertEquals(savedLending.getId(), returnedLending.getId());
        assertNotNull(returnedLending.getReturnedAt());
    }

    @Transactional
    @Test
    void shouldSuccessfullyGetActiveReaders() {
        saveDummyLending("lending_service_active_client", "lending-service-active-isbn");

        Page<Lending> activeReaders = lendingService.getActiveReaders(0, 10);

        assertTrue(activeReaders.getTotalElements() >= 1);
    }

    @Transactional
    @Test
    void shouldThrowIllegalLendingArgumentExceptionWhenLendingCreateRequestIsNull() {
        IllegalLendingArgumentException exception = assertThrows(
                IllegalLendingArgumentException.class,
                () -> lendingService.createLending(null)
        );

        assertEquals("Lending create request is null", exception.getMessage());
    }

    @Transactional
    @Test
    void shouldThrowLendingNotFoundExceptionWhenLendingNotFoundById() {
        LendingNotFoundException exception = assertThrows(
                LendingNotFoundException.class,
                () -> lendingService.getLendingById(Long.MAX_VALUE)
        );

        assertEquals(String.format("Lending with id=%d not found", Long.MAX_VALUE), exception.getMessage());
    }

    @Transactional
    @Test
    void shouldThrowIllegalLendingStateExceptionWhenLendingAlreadyReturned() {
        LendingEntity savedLending = saveDummyLending("lending_service_returned_client", "lending-service-returned-isbn");
        savedLending.setReturnedAt(LocalDateTime.parse("2026-01-02T10:00:00"));
        lendingRepository.saveAndFlush(savedLending);

        IllegalLendingStateException exception = assertThrows(
                IllegalLendingStateException.class,
                () -> lendingService.returnLending(savedLending.getId())
        );

        assertEquals(String.format("Lending with id=%d already returned", savedLending.getId()), exception.getMessage());
    }

    private LendingCreateRequestDto createRequest(Long clientId, Long bookId) {
        LendingCreateRequestDto requestDto = new LendingCreateRequestDto();
        requestDto.setClientId(clientId);
        requestDto.setBookId(bookId);
        requestDto.setTakenAt(LocalDateTime.parse("2026-01-01T10:00:00"));
        return requestDto;
    }

    private LendingEntity saveDummyLending(String clientLogin, String bookIsbn) {
        ClientEntity client = saveDummyClient(clientLogin);
        BookEntity book = saveDummyBook(bookIsbn);
        return lendingRepository.saveAndFlush(LendingEntity.builder()
                .client(client)
                .book(book)
                .takenAt(LocalDateTime.parse("2026-01-01T10:00:00"))
                .build());
    }

    private ClientEntity saveDummyClient(String login) {
        return clientRepository.saveAndFlush(ClientEntity.builder()
                .login(login)
                .password("password")
                .fullName("Dummy Client")
                .birthDate(LocalDate.of(1999, 1, 1))
                .clientRole(ClientRole.USER)
                .build());
    }

    private BookEntity saveDummyBook(String isbn) {
        return bookRepository.saveAndFlush(BookEntity.builder()
                .title("Dummy Book")
                .author("Dummy Author")
                .isbn(isbn)
                .build());
    }
}
