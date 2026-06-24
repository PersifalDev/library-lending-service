package ru.haritonenko.librarylendingservice.lendings.api.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;

@Getter
@Setter
public class LendingCreateRequestDto {

    @NotNull(message = "Lending client id can not be null")
    @Positive(message = "Lending client id must be positive")
    private Long clientId;

    @NotNull(message = "Lending book id can not be null")
    @Positive(message = "Lending book id must be positive")
    private Long bookId;

    private LocalDateTime takenAt;
}
