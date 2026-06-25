package ru.haritonenko.librarylendingservice.clients.api.controller;

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
import ru.haritonenko.librarylendingservice.clients.api.dto.ClientCreateRequestDto;
import ru.haritonenko.librarylendingservice.clients.api.dto.ClientResponseDto;
import ru.haritonenko.librarylendingservice.clients.api.dto.ClientUpdateRequestDto;
import ru.haritonenko.librarylendingservice.clients.api.dto.authorization.ClientCredentials;
import ru.haritonenko.librarylendingservice.clients.domain.Client;
import ru.haritonenko.librarylendingservice.clients.domain.exception.ClientAlreadyExistsException;
import ru.haritonenko.librarylendingservice.clients.domain.exception.ClientNotFoundException;
import ru.haritonenko.librarylendingservice.clients.domain.mapper.ClientMapper;
import ru.haritonenko.librarylendingservice.clients.domain.role.ClientRole;
import ru.haritonenko.librarylendingservice.clients.domain.service.ClientService;
import ru.haritonenko.librarylendingservice.clients.security.jwt.manager.JwtTokenManager;
import ru.haritonenko.librarylendingservice.clients.security.service.AuthenticationService;
import ru.haritonenko.librarylendingservice.config.properties.RateLimitProperties;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ClientControllerWebOnlyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClientService clientService;

    @MockBean
    private ClientMapper clientMapper;

    @MockBean
    private AuthenticationService authenticationService;

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
    void shouldSuccessfullyCreateClient() throws Exception {
        ClientCreateRequestDto requestDto = createClientRequest("client", "password", "Client");
        Client client = createClient(1L, "client", "Client", ClientRole.USER);
        ClientResponseDto responseDto = createResponseDto(1L, "client", "Client", "USER");

        when(clientService.createClient(any(ClientCreateRequestDto.class))).thenReturn(client);
        when(clientMapper.toResponseDto(client)).thenReturn(responseDto);

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.login").value("client"))
                .andExpect(jsonPath("$.clientRole").value("USER"));
    }

    @Test
    void shouldSuccessfullyAuthenticateClient() throws Exception {
        ClientCredentials credentials = new ClientCredentials();
        credentials.setLogin("client");
        credentials.setPassword("password");

        when(authenticationService.authenticate(any(ClientCredentials.class))).thenReturn("jwt-token");

        mockMvc.perform(post("/api/clients/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").value("jwt-token"));
    }

    @Test
    void shouldSuccessfullyGetClientById() throws Exception {
        Client client = createClient(1L, "client", "Client", ClientRole.USER);
        ClientResponseDto responseDto = createResponseDto(1L, "client", "Client", "USER");

        when(clientService.getClientById(1L)).thenReturn(client);
        when(clientMapper.toResponseDto(client)).thenReturn(responseDto);

        mockMvc.perform(get("/api/clients/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.login").value("client"));
    }

    @Test
    void shouldSuccessfullyGetClients() throws Exception {
        Client client = createClient(1L, "client", "Client", ClientRole.USER);
        ClientResponseDto responseDto = createResponseDto(1L, "client", "Client", "USER");

        when(clientService.getClients(eq("Cli"), eq(0), eq(10)))
                .thenReturn(new PageImpl<Client>(Collections.singletonList(client)));
        when(clientMapper.toResponseDto(client)).thenReturn(responseDto);

        mockMvc.perform(get("/api/clients/search")
                        .param("fullName", "Cli")
                        .param("pageNumber", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].login").value("client"));
    }

    @Test
    void shouldSuccessfullyUpdateClient() throws Exception {
        ClientUpdateRequestDto requestDto = new ClientUpdateRequestDto();
        requestDto.setFullName("Updated Client");
        requestDto.setBirthDate(LocalDate.of(1998, 2, 2));
        Client client = createClient(1L, "client", "Updated Client", ClientRole.USER);
        ClientResponseDto responseDto = createResponseDto(1L, "client", "Updated Client", "USER");

        when(clientService.updateClient(eq(1L), any(ClientUpdateRequestDto.class))).thenReturn(client);
        when(clientMapper.toResponseDto(client)).thenReturn(responseDto);

        mockMvc.perform(put("/api/clients/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated Client"));
    }

    @Test
    void shouldReturnBadRequestWhenCreateClientRequestIsInvalid() throws Exception {
        ClientCreateRequestDto requestDto = createClientRequest("", "", "");

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenAuthenticateClientRequestIsInvalid() throws Exception {
        ClientCredentials credentials = new ClientCredentials();
        credentials.setLogin("");
        credentials.setPassword("");

        mockMvc.perform(post("/api/clients/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenUpdateClientRequestIsInvalid() throws Exception {
        ClientUpdateRequestDto requestDto = new ClientUpdateRequestDto();
        requestDto.setFullName("");
        requestDto.setBirthDate(LocalDate.now().plusDays(1));

        mockMvc.perform(put("/api/clients/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenClientIdHasInvalidType() throws Exception {
        mockMvc.perform(get("/api/clients/{id}", "not-a-number"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenClientPageParametersAreInvalid() throws Exception {
        mockMvc.perform(get("/api/clients/search")
                        .param("pageNumber", "-1")
                        .param("pageSize", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation error"));

        mockMvc.perform(get("/api/clients/search")
                        .param("pageNumber", "0")
                        .param("pageSize", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation error"));

        mockMvc.perform(get("/api/clients/search")
                        .param("pageNumber", "0")
                        .param("pageSize", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation error"));
    }

    @Test
    void shouldReturnBadRequestWhenClientIdIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/clients/{id}", 0L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation error"));

        ClientUpdateRequestDto requestDto = new ClientUpdateRequestDto();
        requestDto.setFullName("Updated Client");
        requestDto.setBirthDate(LocalDate.of(1998, 2, 2));

        mockMvc.perform(put("/api/clients/{id}", -1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation error"));
    }

    @Test
    void shouldReturnNotFoundWhenClientDoesNotExist() throws Exception {
        when(clientService.getClientById(404L))
                .thenThrow(new ClientNotFoundException("Client with id=404 not found"));

        mockMvc.perform(get("/api/clients/{id}", 404L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Client not found"));
    }

    @Test
    void shouldReturnConflictWhenClientLoginAlreadyExists() throws Exception {
        ClientCreateRequestDto requestDto = createClientRequest("client", "password", "Client");

        when(clientService.createClient(any(ClientCreateRequestDto.class)))
                .thenThrow(new ClientAlreadyExistsException("Client with login=client is already registered"));

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Client already exists"));
    }

    @Test
    void shouldSuccessfullyGetAuthDebugWhenAuthenticationIsMissing() throws Exception {
        mockMvc.perform(get("/api/clients/debug/auth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").isEmpty())
                .andExpect(jsonPath("$.authorities").isEmpty());
    }

    private ClientCreateRequestDto createClientRequest(String login, String password, String fullName) {
        ClientCreateRequestDto requestDto = new ClientCreateRequestDto();
        requestDto.setLogin(login);
        requestDto.setPassword(password);
        requestDto.setFullName(fullName);
        requestDto.setBirthDate(LocalDate.of(1999, 1, 1));
        return requestDto;
    }

    private Client createClient(Long id, String login, String fullName, ClientRole clientRole) {
        return Client.builder()
                .id(id)
                .login(login)
                .fullName(fullName)
                .birthDate(LocalDate.of(1999, 1, 1))
                .clientRole(clientRole)
                .build();
    }

    private ClientResponseDto createResponseDto(Long id, String login, String fullName, String clientRole) {
        ClientResponseDto responseDto = new ClientResponseDto();
        responseDto.setId(id);
        responseDto.setLogin(login);
        responseDto.setFullName(fullName);
        responseDto.setBirthDate(LocalDate.of(1999, 1, 1));
        responseDto.setClientRole(clientRole);
        return responseDto;
    }
}
