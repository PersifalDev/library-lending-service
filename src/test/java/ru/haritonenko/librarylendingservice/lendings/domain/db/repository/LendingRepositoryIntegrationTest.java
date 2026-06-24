package ru.haritonenko.librarylendingservice.lendings.domain.db.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.haritonenko.librarylendingservice.books.domain.db.entity.BookEntity;
import ru.haritonenko.librarylendingservice.books.domain.db.repository.BookRepository;
import ru.haritonenko.librarylendingservice.clients.domain.db.entity.ClientEntity;
import ru.haritonenko.librarylendingservice.clients.domain.db.repository.ClientRepository;
import ru.haritonenko.librarylendingservice.clients.domain.role.ClientRole;
import ru.haritonenko.librarylendingservice.db.AbstractJpaTest;
import ru.haritonenko.librarylendingservice.lendings.domain.db.entity.LendingEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class LendingRepositoryIntegrationTest extends AbstractJpaTest {

    @Autowired
    private LendingRepository lendingRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private BookRepository bookRepository;

    @Test
    void shouldSaveLendingAndFindWithClientAndBookById() {
        LendingEntity savedLending = lendingRepository.save(createLending("lending-repo-client", "repo-isbn-lending"));

        Optional<LendingEntity> foundLendingOpt = lendingRepository.findWithClientAndBookById(savedLending.getId());

        assertTrue(foundLendingOpt.isPresent());
        assertEquals(savedLending.getId(), foundLendingOpt.get().getId());
        assertNotNull(foundLendingOpt.get().getClient());
        assertNotNull(foundLendingOpt.get().getBook());
    }

    @Test
    void shouldFindActiveLendings() {
        lendingRepository.save(createLending("active-lending-client", "repo-isbn-active"));

        Page<LendingEntity> activeLendings = lendingRepository.findByReturnedAtIsNullOrderByTakenAtDesc(
                PageRequest.of(0, 10)
        );

        assertFalse(activeLendings.isEmpty());
        assertTrue(activeLendings.getContent().stream().allMatch(lending -> lending.getReturnedAt() == null));
    }

    @Test
    void shouldReturnEmptyOptionalWhenLendingNotFoundById() {
        Optional<LendingEntity> foundLendingOpt = lendingRepository.findWithClientAndBookById(Long.MAX_VALUE);

        assertFalse(foundLendingOpt.isPresent());
    }

    private LendingEntity createLending(String clientLogin, String isbn) {
        ClientEntity client = clientRepository.save(ClientEntity.builder()
                .login(clientLogin)
                .password("password")
                .fullName("Repo Lending Client")
                .birthDate(LocalDate.of(1999, 1, 1))
                .clientRole(ClientRole.USER)
                .build());

        BookEntity book = bookRepository.save(BookEntity.builder()
                .title("Repo Lending Book")
                .author("Repo Author")
                .isbn(isbn)
                .build());

        return LendingEntity.builder()
                .client(client)
                .book(book)
                .takenAt(LocalDateTime.parse("2026-01-01T10:00:00"))
                .build();
    }
}
