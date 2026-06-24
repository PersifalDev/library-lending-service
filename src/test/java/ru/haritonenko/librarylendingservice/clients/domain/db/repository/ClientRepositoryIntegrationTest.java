package ru.haritonenko.librarylendingservice.clients.domain.db.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.haritonenko.librarylendingservice.clients.domain.db.entity.ClientEntity;
import ru.haritonenko.librarylendingservice.clients.domain.role.ClientRole;
import ru.haritonenko.librarylendingservice.db.AbstractJpaTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ClientRepositoryIntegrationTest extends AbstractJpaTest {

    @Autowired
    private ClientRepository clientRepository;

    @Test
    void shouldSaveClientAndFindById() {
        ClientEntity client = ClientEntity.builder()
                .login("repo-client")
                .password("password")
                .fullName("Repo Client")
                .birthDate(LocalDate.of(1999, 1, 1))
                .clientRole(ClientRole.USER)
                .build();

        ClientEntity savedClient = clientRepository.save(client);

        Optional<ClientEntity> foundClientOpt = clientRepository.findById(savedClient.getId());

        assertTrue(foundClientOpt.isPresent());
        assertEquals("repo-client", foundClientOpt.get().getLogin());
        assertEquals("Repo Client", foundClientOpt.get().getFullName());
    }

    @Test
    void shouldReturnEmptyOptionalWhenClientNotFoundById() {
        Optional<ClientEntity> foundClientOpt = clientRepository.findById(Long.MAX_VALUE);

        assertFalse(foundClientOpt.isPresent());
    }

    @Test
    void shouldFindClientByLogin() {
        clientRepository.save(createClient("repo-login", "Repo Login"));

        Optional<ClientEntity> foundClientOpt = clientRepository.findByLogin("repo-login");

        assertTrue(foundClientOpt.isPresent());
        assertEquals("Repo Login", foundClientOpt.get().getFullName());
    }

    @Test
    void shouldReturnTrueWhenClientExistsByLogin() {
        clientRepository.save(createClient("repo-existing-login", "Repo Existing"));

        boolean exists = clientRepository.existsByLogin("repo-existing-login");

        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenClientDoesNotExistByLogin() {
        boolean exists = clientRepository.existsByLogin("missing-login");

        assertFalse(exists);
    }

    @Test
    void shouldReturnEmptyOptionalWhenClientNotFoundByLogin() {
        Optional<ClientEntity> foundClientOpt = clientRepository.findByLogin("not-found-login");

        assertFalse(foundClientOpt.isPresent());
    }

    @Test
    void shouldFindClientsByFullNameContainingIgnoreCase() {
        clientRepository.save(createClient("repo-search-one", "Search Target"));
        clientRepository.save(createClient("repo-search-two", "Another Target"));
        clientRepository.save(createClient("repo-search-missing", "Different Name"));

        Page<ClientEntity> foundClients = clientRepository.findByFullNameContainingIgnoreCase(
                "target",
                PageRequest.of(0, 10)
        );

        assertEquals(2, foundClients.getTotalElements());
        assertTrue(foundClients.getContent().stream()
                .allMatch(client -> client.getFullName().toLowerCase().contains("target")));
    }

    private ClientEntity createClient(String login, String fullName) {
        return ClientEntity.builder()
                .login(login)
                .password("password")
                .fullName(fullName)
                .birthDate(LocalDate.of(1999, 1, 1))
                .clientRole(ClientRole.USER)
                .build();
    }
}
