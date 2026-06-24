package ru.haritonenko.librarylendingservice.clients.api.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Getter
@Setter
public class ClientUpdateRequestDto {

    @NotBlank(message = "Client full name can not be blank")
    @Size(max = 255, message = "Max full name size is 255")
    private String fullName;

    @NotNull(message = "Client birth date can not be null")
    @Past(message = "Client birth date must be in the past")
    private LocalDate birthDate;
}
