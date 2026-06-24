package ru.haritonenko.librarylendingservice.lendings.api.controller;

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
import ru.haritonenko.librarylendingservice.clients.security.jwt.manager.JwtTokenManager;
import ru.haritonenko.librarylendingservice.config.properties.RateLimitProperties;
import ru.haritonenko.librarylendingservice.lendings.api.dto.LendingCreateRequestDto;
import ru.haritonenko.librarylendingservice.lendings.api.dto.LendingResponseDto;
import ru.haritonenko.librarylendingservice.lendings.domain.Lending;
import ru.haritonenko.librarylendingservice.lendings.domain.mapper.LendingMapper;
import ru.haritonenko.librarylendingservice.lendings.domain.service.LendingService;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LendingController.class)
@AutoConfigureMockMvc(addFilters = false)
public class LendingControllerWebOnlyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LendingService lendingService;

    @MockBean
    private LendingMapper lendingMapper;

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
    void shouldSuccessfullyCreateLending() throws Exception {
        LendingCreateRequestDto requestDto = new LendingCreateRequestDto();
        requestDto.setClientId(1L);
        requestDto.setBookId(1L);
        requestDto.setTakenAt(LocalDateTime.parse("2026-01-01T10:00:00"));
        Lending lending = createLending(1L);
        LendingResponseDto responseDto = createResponseDto(1L);

        when(lendingService.createLending(any(LendingCreateRequestDto.class))).thenReturn(lending);
        when(lendingMapper.toResponseDto(lending)).thenReturn(responseDto);

        mockMvc.perform(post("/api/lendings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void shouldSuccessfullyGetLendingById() throws Exception {
        Lending lending = createLending(1L);
        LendingResponseDto responseDto = createResponseDto(1L);

        when(lendingService.getLendingById(1L)).thenReturn(lending);
        when(lendingMapper.toResponseDto(lending)).thenReturn(responseDto);

        mockMvc.perform(get("/api/lendings/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void shouldSuccessfullyReturnLending() throws Exception {
        Lending lending = createLending(1L);
        LendingResponseDto responseDto = createResponseDto(1L);

        when(lendingService.returnLending(1L)).thenReturn(lending);
        when(lendingMapper.toResponseDto(lending)).thenReturn(responseDto);

        mockMvc.perform(patch("/api/lendings/{id}/return", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void shouldSuccessfullyGetActiveReaders() throws Exception {
        when(lendingService.getActiveReaders(eq(0), eq(10)))
                .thenReturn(new PageImpl<Lending>(Collections.emptyList()));

        mockMvc.perform(get("/api/lendings/active-readers")
                        .param("pageNumber", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnBadRequestWhenCreateLendingRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/api/lendings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenLendingPageParametersAreInvalid() throws Exception {
        mockMvc.perform(get("/api/lendings/active-readers")
                        .param("pageNumber", "-1")
                        .param("pageSize", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation error"));

        mockMvc.perform(get("/api/lendings/active-readers")
                        .param("pageNumber", "0")
                        .param("pageSize", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation error"));

        mockMvc.perform(get("/api/lendings/active-readers")
                        .param("pageNumber", "0")
                        .param("pageSize", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation error"));
    }

    @Test
    void shouldReturnBadRequestWhenCreateLendingIdsAreNotPositive() throws Exception {
        LendingCreateRequestDto requestDto = new LendingCreateRequestDto();
        requestDto.setClientId(0L);
        requestDto.setBookId(-1L);

        mockMvc.perform(post("/api/lendings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation error"));
    }

    @Test
    void shouldReturnBadRequestWhenLendingIdIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/lendings/{id}", 0L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation error"));

        mockMvc.perform(patch("/api/lendings/{id}/return", -1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation error"));
    }

    private Lending createLending(Long id) {
        return Lending.builder()
                .id(id)
                .takenAt(LocalDateTime.parse("2026-01-01T10:00:00"))
                .build();
    }

    private LendingResponseDto createResponseDto(Long id) {
        LendingResponseDto responseDto = new LendingResponseDto();
        responseDto.setId(id);
        responseDto.setTakenAt(LocalDateTime.parse("2026-01-01T10:00:00"));
        return responseDto;
    }
}
