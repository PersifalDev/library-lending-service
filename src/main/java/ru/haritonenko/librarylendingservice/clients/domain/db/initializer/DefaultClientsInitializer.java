package ru.haritonenko.librarylendingservice.clients.domain.db.initializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.haritonenko.librarylendingservice.clients.domain.db.entity.ClientEntity;
import ru.haritonenko.librarylendingservice.clients.domain.db.repository.ClientRepository;
import ru.haritonenko.librarylendingservice.clients.domain.role.ClientRole;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultClientsInitializer {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        createIfNotExists("admin", "admin_password", "Admin Client", LocalDate.of(1990, 1, 1), ClientRole.ADMIN);
        createIfNotExists("pavlov", "pavlov_password", "Pavel Pavlov", LocalDate.of(1998, 7, 22), ClientRole.USER);
    }

    private void createIfNotExists(
            String login,
            String password,
            String fullName,
            LocalDate birthDate,
            ClientRole role
    ) {
        if (clientRepository.existsByLogin(login)) {
            log.warn("Default client '{}' already exists, skipping", login);
            return;
        }

        ClientEntity entity = ClientEntity.builder()
                .login(login)
                .password(passwordEncoder.encode(password))
                .fullName(fullName)
                .birthDate(birthDate)
                .clientRole(role)
                .build();

        clientRepository.save(entity);
        log.info("Default client '{}' created with role {}", login, role);
    }
}
