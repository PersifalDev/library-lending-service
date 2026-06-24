package ru.haritonenko.librarylendingservice.clients.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.redis.core.RedisTemplate;
import ru.haritonenko.librarylendingservice.clients.api.dto.ClientCreateRequestDto;
import ru.haritonenko.librarylendingservice.clients.api.dto.ClientUpdateRequestDto;
import ru.haritonenko.librarylendingservice.clients.domain.Client;
import ru.haritonenko.librarylendingservice.clients.domain.db.entity.ClientEntity;
import ru.haritonenko.librarylendingservice.clients.domain.db.repository.ClientRepository;
import ru.haritonenko.librarylendingservice.clients.domain.exception.ClientNotFoundException;
import ru.haritonenko.librarylendingservice.clients.domain.exception.IllegalClientArgumentException;
import ru.haritonenko.librarylendingservice.clients.domain.mapper.ClientMapper;
import ru.haritonenko.librarylendingservice.clients.domain.role.ClientRole;
import ru.haritonenko.librarylendingservice.config.PageConfig;
import ru.haritonenko.librarylendingservice.config.properties.CacheProperties;
import ru.haritonenko.librarylendingservice.config.properties.SearchProperties;
import ru.haritonenko.librarylendingservice.lendings.domain.service.LendingCacheService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceUnitTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private ClientMapper clientMapper;
    @Mock
    private PageConfig pageConfig;
    @Mock
    private ObjectProvider<RedisTemplate<String, Client>> redisClientTemplateProvider;
    @Mock
    private CacheProperties cacheProperties;
    @Mock
    private SearchProperties searchProperties;
    @Mock
    private LendingCacheService lendingCacheService;
    @Mock
    private PasswordEncoder passwordEncoder;

    private ClientService clientService;
    private ClientEntity clientEntity;
    private Client clientDomain;
    private ClientCreateRequestDto clientCreateRequestDto;

    @BeforeEach
    void setUp() {
        when(redisClientTemplateProvider.getIfAvailable()).thenReturn(null);
        clientService = new ClientService(
                clientRepository,
                clientMapper,
                pageConfig,
                redisClientTemplateProvider,
                cacheProperties,
                searchProperties,
                lendingCacheService,
                passwordEncoder
        );
        clientEntity = ClientEntity.builder()
                .id(1L)
                .login("client")
                .password("hash")
                .fullName("Client")
                .birthDate(LocalDate.of(1999, 1, 1))
                .clientRole(ClientRole.USER)
                .build();
        clientDomain = Client.builder()
                .id(1L)
                .login("client")
                .fullName("Client")
                .birthDate(LocalDate.of(1999, 1, 1))
                .clientRole(ClientRole.USER)
                .build();
        clientCreateRequestDto = createClientRequest("client", "password", "Client");
    }

    @Test
    void shouldSuccessfullyCreateClient() {
        when(clientRepository.existsByLogin("client")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hash");
        when(clientRepository.save(any(ClientEntity.class))).thenReturn(clientEntity);
        when(clientMapper.toDomain(clientEntity)).thenReturn(clientDomain);

        Client createdClient = clientService.createClient(clientCreateRequestDto);

        assertEquals(1L, createdClient.getId());
        assertEquals("client", createdClient.getLogin());
        assertEquals(ClientRole.USER, createdClient.getClientRole());

        verify(clientRepository).existsByLogin("client");
        verify(passwordEncoder).encode("password");
        verify(clientRepository).save(any(ClientEntity.class));
        verify(clientMapper).toDomain(clientEntity);
    }

    @Test
    void shouldSuccessfullyGetClientById() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(clientEntity));
        when(clientMapper.toDomain(clientEntity)).thenReturn(clientDomain);

        Client foundClient = clientService.getClientById(1L);

        assertEquals(clientDomain.getId(), foundClient.getId());
        assertEquals(clientDomain.getLogin(), foundClient.getLogin());

        verify(clientRepository).findById(1L);
        verify(clientMapper).toDomain(clientEntity);
        verify(redisClientTemplateProvider).getIfAvailable();
    }

    @Test
    void shouldSuccessfullyFindClientByLogin() {
        when(clientRepository.findByLogin("client")).thenReturn(Optional.of(clientEntity));
        when(clientMapper.toDomain(clientEntity)).thenReturn(clientDomain);

        Client foundClient = clientService.findByLogin("client");

        assertEquals(clientDomain.getId(), foundClient.getId());
        assertEquals(clientDomain.getLogin(), foundClient.getLogin());

        verify(clientRepository).findByLogin("client");
        verify(clientMapper).toDomain(clientEntity);
    }

    @Test
    void shouldSuccessfullyGetClients() {
        when(pageConfig.pageable(0, 10)).thenReturn(PageRequest.of(0, 10));
        when(clientRepository.findByFullNameContainingIgnoreCase("Cli", PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(Collections.singletonList(clientEntity)));
        when(clientMapper.toDomain(clientEntity)).thenReturn(clientDomain);

        Page<Client> clients = clientService.getClients("Cli", 0, 10);

        assertEquals(1, clients.getTotalElements());
        assertEquals("client", clients.getContent().get(0).getLogin());

        verify(pageConfig).pageable(0, 10);
        verify(clientRepository).findByFullNameContainingIgnoreCase("Cli", PageRequest.of(0, 10));
    }

    @Test
    void shouldSuccessfullyUpdateClient() {
        ClientUpdateRequestDto requestDto = new ClientUpdateRequestDto();
        requestDto.setFullName("Updated Client");
        requestDto.setBirthDate(LocalDate.of(1998, 2, 2));

        ClientEntity updatedEntity = ClientEntity.builder()
                .id(1L)
                .login("client")
                .password("hash")
                .fullName("Updated Client")
                .birthDate(LocalDate.of(1998, 2, 2))
                .clientRole(ClientRole.USER)
                .build();
        Client updatedDomain = Client.builder()
                .id(1L)
                .login("client")
                .fullName("Updated Client")
                .birthDate(LocalDate.of(1998, 2, 2))
                .clientRole(ClientRole.USER)
                .build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(clientEntity));
        when(clientRepository.save(clientEntity)).thenReturn(updatedEntity);
        when(clientMapper.toDomain(updatedEntity)).thenReturn(updatedDomain);

        Client updatedClient = clientService.updateClient(1L, requestDto);

        assertEquals("Updated Client", updatedClient.getFullName());
        assertEquals(LocalDate.of(1998, 2, 2), updatedClient.getBirthDate());

        verify(clientRepository).findById(1L);
        verify(clientRepository).save(clientEntity);
        verify(lendingCacheService).invalidateByClientId(1L);
    }

    @Test
    void shouldThrowIllegalClientArgumentExceptionWhenClientCreateRequestIsNull() {
        IllegalClientArgumentException exception = assertThrows(
                IllegalClientArgumentException.class,
                () -> clientService.createClient(null)
        );

        assertEquals("Client create request is null", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalClientArgumentExceptionWhenClientWithLoginAlreadyExists() {
        when(clientRepository.existsByLogin("client")).thenReturn(true);

        IllegalClientArgumentException exception = assertThrows(
                IllegalClientArgumentException.class,
                () -> clientService.createClient(clientCreateRequestDto)
        );

        assertEquals("Client with login=client is already registered", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalClientArgumentExceptionWhenClientIdIsNull() {
        IllegalClientArgumentException exception = assertThrows(
                IllegalClientArgumentException.class,
                () -> clientService.getClientById(null)
        );

        assertEquals("Client id is null", exception.getMessage());
    }

    @Test
    void shouldThrowClientNotFoundExceptionWhenClientNotFoundById() {
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        ClientNotFoundException exception = assertThrows(
                ClientNotFoundException.class,
                () -> clientService.getClientById(999L)
        );

        assertEquals("Client with id=999 not found", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalClientArgumentExceptionWhenClientLoginIsNull() {
        IllegalClientArgumentException exception = assertThrows(
                IllegalClientArgumentException.class,
                () -> clientService.findByLogin(null)
        );

        assertEquals("Client login is null or blank", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalClientArgumentExceptionWhenClientLoginIsBlank() {
        IllegalClientArgumentException exception = assertThrows(
                IllegalClientArgumentException.class,
                () -> clientService.findByLogin(" ")
        );

        assertEquals("Client login is null or blank", exception.getMessage());
    }

    @Test
    void shouldThrowClientNotFoundExceptionWhenClientNotFoundByLogin() {
        when(clientRepository.findByLogin("unknown")).thenReturn(Optional.empty());

        ClientNotFoundException exception = assertThrows(
                ClientNotFoundException.class,
                () -> clientService.findByLogin("unknown")
        );

        assertEquals("Client with login=unknown not found", exception.getMessage());
    }

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

    private ClientCreateRequestDto createClientRequest(String login, String password, String fullName) {
        ClientCreateRequestDto requestDto = new ClientCreateRequestDto();
        requestDto.setLogin(login);
        requestDto.setPassword(password);
        requestDto.setFullName(fullName);
        requestDto.setBirthDate(LocalDate.of(1999, 1, 1));
        return requestDto;
    }
}
