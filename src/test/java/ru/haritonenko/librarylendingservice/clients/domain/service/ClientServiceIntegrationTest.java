package ru.haritonenko.librarylendingservice.clients.domain.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import ru.haritonenko.librarylendingservice.clients.api.dto.ClientCreateRequestDto;
import ru.haritonenko.librarylendingservice.clients.api.dto.ClientUpdateRequestDto;
import ru.haritonenko.librarylendingservice.clients.domain.Client;
import ru.haritonenko.librarylendingservice.clients.domain.db.entity.ClientEntity;
import ru.haritonenko.librarylendingservice.clients.domain.db.repository.ClientRepository;
import ru.haritonenko.librarylendingservice.clients.domain.exception.ClientNotFoundException;
import ru.haritonenko.librarylendingservice.clients.domain.exception.IllegalClientArgumentException;
import ru.haritonenko.librarylendingservice.clients.domain.role.ClientRole;
import ru.haritonenko.librarylendingservice.integration.AbstractIntegrationTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class ClientServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ClientService clientService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    @Test
    void shouldSuccessfullyCreateClient() {
        ClientCreateRequestDto requestDto = new ClientCreateRequestDto();
        requestDto.setLogin("integration-client");
        requestDto.setPassword("password");
        requestDto.setFullName("Integration Client");
        requestDto.setBirthDate(LocalDate.of(1999, 1, 1));

        Client createdClient = clientService.createClient(requestDto);

        assertNotNull(createdClient.getId());
        assertEquals("integration-client", createdClient.getLogin());
        assertEquals("Integration Client", createdClient.getFullName());
        assertEquals(ClientRole.USER, createdClient.getClientRole());

        ClientEntity foundClientEntity = clientRepository.findById(createdClient.getId())
                .orElseThrow(() -> new AssertionError("Created client was not found"));
        assertEquals("integration-client", foundClientEntity.getLogin());
        assertTrue(passwordEncoder.matches("password", foundClientEntity.getPassword()));
    }

    @Transactional
    @Test
    void shouldSuccessfullyGetClientById() {
        ClientEntity savedClientEntity = saveDummyClient("client_get_by_id");

        Client foundClient = clientService.getClientById(savedClientEntity.getId());

        assertEquals(savedClientEntity.getId(), foundClient.getId());
        assertEquals(savedClientEntity.getLogin(), foundClient.getLogin());
        assertEquals(savedClientEntity.getFullName(), foundClient.getFullName());
        assertEquals(savedClientEntity.getBirthDate(), foundClient.getBirthDate());
        assertEquals(savedClientEntity.getClientRole(), foundClient.getClientRole());
    }

    @Transactional
    @Test
    void shouldSuccessfullyFindByLogin() {
        ClientEntity savedClientEntity = saveDummyClient("client_find_login");

        Client foundClient = clientService.findByLogin(savedClientEntity.getLogin());

        assertEquals(savedClientEntity.getId(), foundClient.getId());
        assertEquals(savedClientEntity.getLogin(), foundClient.getLogin());
    }

    @Transactional
    @Test
    void shouldSuccessfullyUpdateClient() {
        ClientEntity savedClientEntity = saveDummyClient("client_update");
        ClientUpdateRequestDto requestDto = new ClientUpdateRequestDto();
        requestDto.setFullName("Updated Integration Client");
        requestDto.setBirthDate(LocalDate.of(1998, 2, 2));

        Client updatedClient = clientService.updateClient(savedClientEntity.getId(), requestDto);

        assertEquals(savedClientEntity.getId(), updatedClient.getId());
        assertEquals("Updated Integration Client", updatedClient.getFullName());
        assertEquals(LocalDate.of(1998, 2, 2), updatedClient.getBirthDate());

        ClientEntity foundClientEntity = clientRepository.findById(savedClientEntity.getId())
                .orElseThrow(() -> new AssertionError("Updated client was not found"));
        assertEquals("Updated Integration Client", foundClientEntity.getFullName());
        assertEquals(LocalDate.of(1998, 2, 2), foundClientEntity.getBirthDate());
    }

    @Transactional
    @Test
    void shouldThrowIllegalClientArgumentExceptionWhenClientIdIsNull() {
        IllegalClientArgumentException exception = assertThrows(
                IllegalClientArgumentException.class,
                () -> clientService.getClientById(null)
        );

        assertEquals("Client id is null", exception.getMessage());
    }

    @Transactional
    @Test
    void shouldThrowClientNotFoundExceptionWhenClientNotFoundById() {
        ClientNotFoundException exception = assertThrows(
                ClientNotFoundException.class,
                () -> clientService.getClientById(Long.MAX_VALUE)
        );

        assertEquals(String.format("Client with id=%d not found", Long.MAX_VALUE), exception.getMessage());
    }

    @Transactional
    @Test
    void shouldThrowIllegalClientArgumentExceptionWhenClientCreateRequestIsNull() {
        IllegalClientArgumentException exception = assertThrows(
                IllegalClientArgumentException.class,
                () -> clientService.createClient(null)
        );

        assertEquals("Client create request is null", exception.getMessage());
    }

    @Transactional
    @Test
    void shouldThrowIllegalClientArgumentExceptionWhenClientAlreadyRegistered() {
        saveDummyClient("client_already_exists");

        ClientCreateRequestDto requestDto = new ClientCreateRequestDto();
        requestDto.setLogin("client_already_exists");
        requestDto.setPassword("password");
        requestDto.setFullName("Duplicate Client");
        requestDto.setBirthDate(LocalDate.of(1999, 1, 1));

        IllegalClientArgumentException exception = assertThrows(
                IllegalClientArgumentException.class,
                () -> clientService.createClient(requestDto)
        );

        assertEquals("Client with login=client_already_exists is already registered", exception.getMessage());
    }

    @Transactional
    @Test
    void shouldThrowIllegalClientArgumentExceptionWhenLoginIsNullOrBlank() {
        IllegalClientArgumentException exceptionForNull = assertThrows(
                IllegalClientArgumentException.class,
                () -> clientService.findByLogin(null)
        );
        assertEquals("Client login is null or blank", exceptionForNull.getMessage());

        IllegalClientArgumentException exceptionForBlank = assertThrows(
                IllegalClientArgumentException.class,
                () -> clientService.findByLogin(" ")
        );
        assertEquals("Client login is null or blank", exceptionForBlank.getMessage());
    }

    @Transactional
    @Test
    void shouldThrowClientNotFoundExceptionWhenClientNotFoundByLogin() {
        ClientNotFoundException exception = assertThrows(
                ClientNotFoundException.class,
                () -> clientService.findByLogin("missing-login")
        );

        assertEquals("Client with login=missing-login not found", exception.getMessage());
    }

    @Transactional
    @Test
    void shouldThrowIllegalClientArgumentExceptionWhenClientIdOrUpdateRequestIsNull() {
        ClientUpdateRequestDto requestDto = new ClientUpdateRequestDto();

        IllegalClientArgumentException exceptionForNullId = assertThrows(
                IllegalClientArgumentException.class,
                () -> clientService.updateClient(null, requestDto)
        );
        assertEquals("Client id or update request is null", exceptionForNullId.getMessage());

        IllegalClientArgumentException exceptionForNullRequest = assertThrows(
                IllegalClientArgumentException.class,
                () -> clientService.updateClient(1L, null)
        );
        assertEquals("Client id or update request is null", exceptionForNullRequest.getMessage());
    }

    private ClientEntity saveDummyClient(String login) {
        ClientEntity clientEntity = ClientEntity.builder()
                .login(login)
                .password(passwordEncoder.encode("password123"))
                .fullName("Dummy Client")
                .birthDate(LocalDate.of(1999, 1, 1))
                .clientRole(ClientRole.USER)
                .build();
        return clientRepository.saveAndFlush(clientEntity);
    }
}
