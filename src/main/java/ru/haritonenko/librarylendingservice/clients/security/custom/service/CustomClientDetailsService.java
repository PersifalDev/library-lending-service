package ru.haritonenko.librarylendingservice.clients.security.custom.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import ru.haritonenko.librarylendingservice.clients.domain.db.entity.ClientEntity;
import ru.haritonenko.librarylendingservice.clients.domain.db.repository.ClientRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomClientDetailsService implements UserDetailsService {

    private final ClientRepository clientRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading client by login={}", username);

        ClientEntity client = clientRepository.findByLogin(username)
                .orElseThrow(() -> {
                    log.warn("Client with login={} not found", username);
                    return new UsernameNotFoundException(String.format("Client not found by login: %s", username));
                });

        return User.withUsername(username)
                .password(client.getPassword())
                .authorities(String.valueOf(client.getClientRole()))
                .build();
    }
}
