package ru.haritonenko.librarylendingservice.books.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookResponseDto {

    private Long id;
    private String title;
    private String author;
    private String isbn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
