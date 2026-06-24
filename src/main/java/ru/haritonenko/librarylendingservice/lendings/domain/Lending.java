package ru.haritonenko.librarylendingservice.lendings.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.haritonenko.librarylendingservice.books.domain.Book;
import ru.haritonenko.librarylendingservice.clients.domain.Client;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lending implements Serializable {

    private Long id;
    private Client client;
    private Book book;
    private LocalDateTime takenAt;
    private LocalDateTime returnedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
