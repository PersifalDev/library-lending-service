package ru.haritonenko.librarylendingservice.clients.security.jwt.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JwtResponse {

    private final String jwt;
}
