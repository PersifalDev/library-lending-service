package ru.haritonenko.librarylendingservice.lendings.domain.exception;

public class IllegalLendingStateException extends RuntimeException {

    public IllegalLendingStateException(String message) {
        super(message);
    }
}
