package ru.haritonenko.librarylendingservice.clients.security.custom.authentification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthClient implements Serializable {

    @NotNull(message = "Client id can not be null")
    private Long id;

    @NotBlank(message = "Client login can not be blank")
    private String login;

    @NotBlank(message = "Client role can not be blank")
    private String role;
}
