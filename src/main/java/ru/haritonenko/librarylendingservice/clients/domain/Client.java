package ru.haritonenko.librarylendingservice.clients.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.haritonenko.librarylendingservice.clients.domain.role.ClientRole;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Client implements Serializable {

    private Long id;
    private String login;
    private String password;
    private String fullName;
    private LocalDate birthDate;
    private ClientRole clientRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
