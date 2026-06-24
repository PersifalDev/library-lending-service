package ru.haritonenko.librarylendingservice.lendings.domain.exception;

public class IllegalLendingArgumentException extends RuntimeException {

    public IllegalLendingArgumentException(String message) {
        super(message);
    }
}
