package ru.haritonenko.librarylendingservice.books.domain.exception;

public class BookAlreadyExistsException extends RuntimeException {

    public BookAlreadyExistsException(String message) {
        super(message);
    }
}
