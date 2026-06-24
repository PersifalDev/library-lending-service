package ru.haritonenko.librarylendingservice.clients.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import ru.haritonenko.librarylendingservice.clients.api.dto.authorization.ClientCredentials;
import ru.haritonenko.librarylendingservice.clients.domain.Client;
import ru.haritonenko.librarylendingservice.clients.domain.exception.IllegalClientArgumentException;
import ru.haritonenko.librarylendingservice.clients.security.jwt.manager.JwtTokenManager;
import ru.haritonenko.librarylendingservice.clients.domain.service.ClientService;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenManager jwtTokenManager;
    private final ClientService clientService;

    public String authenticate(ClientCredentials clientFromSignInRequest) {
        if (clientFromSignInRequest == null) {
            log.warn("Client authentication request is null");
            throw new IllegalClientArgumentException("Client authentication request is null");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        clientFromSignInRequest.getLogin(),
                        clientFromSignInRequest.getPassword()
                )
        );

        Client client = clientService.findByLogin(clientFromSignInRequest.getLogin());
        return jwtTokenManager.generateToken(
                client.getId(),
                client.getLogin(),
                client.getClientRole().toString()
        );
    }
}
