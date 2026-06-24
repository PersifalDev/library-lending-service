package ru.haritonenko.librarylendingservice.clients.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientResponseDto {

    private Long id;
    private String login;
    private String fullName;
    private LocalDate birthDate;
    private String clientRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
