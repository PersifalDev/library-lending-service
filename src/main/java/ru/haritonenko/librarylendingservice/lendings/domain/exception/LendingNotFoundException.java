package ru.haritonenko.librarylendingservice.lendings.domain.exception;

public class LendingNotFoundException extends RuntimeException {

    public LendingNotFoundException(String message) {
        super(message);
    }
}
