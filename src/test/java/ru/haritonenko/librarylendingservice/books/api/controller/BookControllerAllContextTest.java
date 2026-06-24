package ru.haritonenko.librarylendingservice.books.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.haritonenko.librarylendingservice.books.api.dto.BookCreateRequestDto;
import ru.haritonenko.librarylendingservice.books.api.dto.BookUpdateRequestDto;
import ru.haritonenko.librarylendingservice.integration.AbstractIntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BookControllerAllContextTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldSuccessfullyCreateBook() throws Exception {
        BookCreateRequestDto requestDto = createBookRequest("controller-book-create", "controller-author-create", "controller-isbn-create");

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("controller-book-create"))
                .andExpect(jsonPath("$.author").value("controller-author-create"))
                .andExpect(jsonPath("$.isbn").value("controller-isbn-create"));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void shouldReturnForbiddenWhenCreateBookWithoutAdminAuthority() throws Exception {
        BookCreateRequestDto requestDto = createBookRequest("controller-book-forbidden", "controller-author", "controller-isbn-forbidden");

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWhenCreateBookWithoutAuthentication() throws Exception {
        BookCreateRequestDto requestDto = createBookRequest("controller-book-unauthorized", "controller-author", "controller-isbn-unauthorized");

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void shouldSuccessfullyGetBookById() throws Exception {
        Long bookId = createBookAndReturnId("controller-book-get", "controller-author-get", "controller-isbn-get");

        mockMvc.perform(get("/api/books/{id}", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookId))
                .andExpect(jsonPath("$.title").value("controller-book-get"));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void shouldSuccessfullyGetBooks() throws Exception {
        mockMvc.perform(get("/api/books/search")
                        .param("pageNumber", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldSuccessfullyUpdateBook() throws Exception {
        Long bookId = createBookAndReturnId("controller-book-update", "controller-author-update", "controller-isbn-update");
        BookUpdateRequestDto requestDto = new BookUpdateRequestDto();
        requestDto.setTitle("controller-book-updated");
        requestDto.setAuthor("controller-author-updated");
        requestDto.setIsbn("controller-isbn-updated");

        mockMvc.perform(put("/api/books/{id}", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("controller-book-updated"))
                .andExpect(jsonPath("$.author").value("controller-author-updated"))
                .andExpect(jsonPath("$.isbn").value("controller-isbn-updated"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldReturnBadRequestWhenCreateBookRequestIsInvalid() throws Exception {
        BookCreateRequestDto requestDto = createBookRequest("", "", "");

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void shouldReturnNotFoundWhenBookDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/books/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    private BookCreateRequestDto createBookRequest(String title, String author, String isbn) {
        BookCreateRequestDto requestDto = new BookCreateRequestDto();
        requestDto.setTitle(title);
        requestDto.setAuthor(author);
        requestDto.setIsbn(isbn);
        return requestDto;
    }

    private Long createBookAndReturnId(String title, String author, String isbn) throws Exception {
        BookCreateRequestDto requestDto = createBookRequest(title, author, isbn);

        MvcResult result = mockMvc.perform(post("/api/books")
                        .with(user("admin").authorities(new SimpleGrantedAuthority("ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
}
