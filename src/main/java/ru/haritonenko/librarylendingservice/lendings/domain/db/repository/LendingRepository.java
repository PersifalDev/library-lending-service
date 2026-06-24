package ru.haritonenko.librarylendingservice.lendings.domain.db.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.haritonenko.librarylendingservice.lendings.domain.db.entity.LendingEntity;

import java.util.Optional;

public interface LendingRepository extends JpaRepository<LendingEntity, Long> {

    @EntityGraph(attributePaths = {"client", "book"})
    Optional<LendingEntity> findWithClientAndBookById(Long id);

    @EntityGraph(attributePaths = {"client", "book"})
    Page<LendingEntity> findByReturnedAtIsNullOrderByTakenAtDesc(Pageable pageable);
}
