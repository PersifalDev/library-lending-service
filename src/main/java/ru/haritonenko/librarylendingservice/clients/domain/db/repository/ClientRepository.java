package ru.haritonenko.librarylendingservice.clients.domain.db.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.haritonenko.librarylendingservice.clients.domain.db.entity.ClientEntity;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<ClientEntity, Long> {

    boolean existsByLogin(String login);

    Optional<ClientEntity> findByLogin(String login);

    @Query("select c from ClientEntity c where lower(c.fullName) like lower(concat('%', :fullName, '%'))")
    Page<ClientEntity> findByFullNameContainingIgnoreCase(@Param("fullName") String fullName, Pageable pageable);
}
