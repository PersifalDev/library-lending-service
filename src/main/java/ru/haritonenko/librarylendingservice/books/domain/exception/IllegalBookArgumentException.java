package ru.haritonenko.librarylendingservice.books.domain.exception;

public class IllegalBookArgumentException extends RuntimeException {

    public IllegalBookArgumentException(String message) {
        super(message);
    }
}
