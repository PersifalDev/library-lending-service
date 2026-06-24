package ru.haritonenko.librarylendingservice.lendings.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import ru.haritonenko.librarylendingservice.books.api.dto.BookResponseDto;
import ru.haritonenko.librarylendingservice.clients.api.dto.ClientResponseDto;

import java.time.LocalDateTime;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LendingResponseDto {

    private Long id;
    private ClientResponseDto client;
    private BookResponseDto book;
    private LocalDateTime takenAt;
    private LocalDateTime returnedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
