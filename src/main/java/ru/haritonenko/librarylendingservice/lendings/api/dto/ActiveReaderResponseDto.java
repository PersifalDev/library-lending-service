package ru.haritonenko.librarylendingservice.lendings.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActiveReaderResponseDto {

    private String clientFullName;
    private LocalDate clientBirthDate;
    private String bookTitle;
    private String bookAuthor;
    private String bookIsbn;
    private LocalDateTime takenAt;
}
