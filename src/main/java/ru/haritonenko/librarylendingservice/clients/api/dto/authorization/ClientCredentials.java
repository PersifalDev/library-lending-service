package ru.haritonenko.librarylendingservice.clients.api.dto.authorization;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientCredentials {

    @NotBlank(message = "Client login can not be blank")
    @Size(min = 4, max = 50, message = "Min login size is 4, max is 50")
    private String login;

    @NotBlank(message = "Client password can not be blank")
    @Size(min = 4, max = 50, message = "Min password size is 4, max is 50")
    private String password;
}
