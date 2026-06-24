package ru.haritonenko.librarylendingservice.books.domain.db.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.haritonenko.librarylendingservice.books.domain.db.entity.BookEntity;

import java.util.Optional;

public interface BookRepository extends JpaRepository<BookEntity, Long> {

    boolean existsByIsbn(String isbn);

    Optional<BookEntity> findByIsbn(String isbn);

    @Query("select b from BookEntity b " +
            "where lower(b.title) like lower(concat('%', :title, '%')) " +
            "and lower(b.author) like lower(concat('%', :author, '%'))")
    Page<BookEntity> findByTitleContainingIgnoreCaseAndAuthorContainingIgnoreCase(
            @Param("title") String title,
            @Param("author") String author,
            Pageable pageable
    );
}
