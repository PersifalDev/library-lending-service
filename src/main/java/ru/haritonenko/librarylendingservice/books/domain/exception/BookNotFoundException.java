package ru.haritonenko.librarylendingservice.books.domain.exception;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(String message) {
        super(message);
    }
}
