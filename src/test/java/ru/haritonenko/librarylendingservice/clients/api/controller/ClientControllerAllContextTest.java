package ru.haritonenko.librarylendingservice.clients.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.haritonenko.librarylendingservice.clients.api.dto.ClientCreateRequestDto;
import ru.haritonenko.librarylendingservice.clients.api.dto.ClientUpdateRequestDto;
import ru.haritonenko.librarylendingservice.clients.api.dto.authorization.ClientCredentials;
import ru.haritonenko.librarylendingservice.integration.AbstractIntegrationTest;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClientControllerAllContextTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSuccessfullyCreateClientWithoutAuthentication() throws Exception {
        ClientCreateRequestDto requestDto = createClientRequest(
                "controller-client",
                "password",
                "Controller Client"
        );

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.login").value("controller-client"))
                .andExpect(jsonPath("$.fullName").value("Controller Client"))
                .andExpect(jsonPath("$.clientRole").value("USER"));
    }

    @Test
    void shouldSuccessfullyAuthenticateClientWithoutAuthentication() throws Exception {
        ClientCreateRequestDto requestDto = createClientRequest(
                "controller-auth-client",
                "password",
                "Controller Auth Client"
        );
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());

        ClientCredentials credentials = new ClientCredentials();
        credentials.setLogin("controller-auth-client");
        credentials.setPassword("password");

        mockMvc.perform(post("/api/clients/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").isNotEmpty());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldSuccessfullyGetClientById() throws Exception {
        Long clientId = createClientAndReturnId("controller-get-client");

        mockMvc.perform(get("/api/clients/{id}", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clientId));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldSuccessfullyGetClients() throws Exception {
        mockMvc.perform(get("/api/clients/search")
                        .param("pageNumber", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldSuccessfullyUpdateClient() throws Exception {
        Long clientId = createClientAndReturnId("controller-update-client");
        ClientUpdateRequestDto requestDto = new ClientUpdateRequestDto();
        requestDto.setFullName("Updated Controller Client");
        requestDto.setBirthDate(LocalDate.of(1998, 2, 2));

        mockMvc.perform(put("/api/clients/{id}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated Controller Client"));
    }

    @Test
    void shouldReturnUnauthorizedWhenGetClientsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/clients/search"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void shouldReturnForbiddenWhenGetClientsWithoutAdminAuthority() throws Exception {
        mockMvc.perform(get("/api/clients/search"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldSuccessfullyGetAuthDebugWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/clients/debug/auth"))
                .andExpect(status().isOk());
    }

    private ClientCreateRequestDto createClientRequest(String login, String password, String fullName) {
        ClientCreateRequestDto requestDto = new ClientCreateRequestDto();
        requestDto.setLogin(login);
        requestDto.setPassword(password);
        requestDto.setFullName(fullName);
        requestDto.setBirthDate(LocalDate.of(1999, 1, 1));
        return requestDto;
    }

    private Long createClientAndReturnId(String login) throws Exception {
        ClientCreateRequestDto requestDto = createClientRequest(login, "password", "Controller Client");

        MvcResult result = mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
}
