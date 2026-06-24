package ru.haritonenko.librarylendingservice.lendings.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.haritonenko.librarylendingservice.integration.AbstractIntegrationTest;
import ru.haritonenko.librarylendingservice.lendings.api.dto.LendingCreateRequestDto;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LendingControllerAllContextTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(authorities = "USER")
    void shouldSuccessfullyCreateLending() throws Exception {
        LendingCreateRequestDto requestDto = createLendingRequest(1L, 1L);

        mockMvc.perform(post("/api/lendings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void shouldSuccessfullyGetLendingById() throws Exception {
        Long lendingId = createLendingAndReturnId(2L, 2L);

        mockMvc.perform(get("/api/lendings/{id}", lendingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(lendingId));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void shouldSuccessfullyReturnLending() throws Exception {
        Long lendingId = createLendingAndReturnId(3L, 3L);

        mockMvc.perform(patch("/api/lendings/{id}/return", lendingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(lendingId));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void shouldSuccessfullyGetActiveReaders() throws Exception {
        mockMvc.perform(get("/api/lendings/active-readers")
                        .param("pageNumber", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnUnauthorizedWhenGetActiveReadersWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/lendings/active-readers"))
                .andExpect(status().isUnauthorized());
    }

    private Long createLendingAndReturnId(Long clientId, Long bookId) throws Exception {
        LendingCreateRequestDto requestDto = createLendingRequest(clientId, bookId);

        MvcResult result = mockMvc.perform(post("/api/lendings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private LendingCreateRequestDto createLendingRequest(Long clientId, Long bookId) {
        LendingCreateRequestDto requestDto = new LendingCreateRequestDto();
        requestDto.setClientId(clientId);
        requestDto.setBookId(bookId);
        requestDto.setTakenAt(LocalDateTime.parse("2026-01-01T10:00:00"));
        return requestDto;
    }
}
