package ru.haritonenko.librarylendingservice.clients.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ClientAuthDebugResponse {

    private final String name;
    private final List<String> authorities;
}
