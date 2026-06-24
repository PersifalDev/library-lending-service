package ru.haritonenko.librarylendingservice.clients.domain.exception;

public class IllegalClientArgumentException extends RuntimeException {

    public IllegalClientArgumentException(String message) {
        super(message);
    }
}
