package ru.haritonenko.librarylendingservice.books.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book implements Serializable {

    private Long id;
    private String title;
    private String author;
    private String isbn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
