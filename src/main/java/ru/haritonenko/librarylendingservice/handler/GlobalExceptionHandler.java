package ru.haritonenko.librarylendingservice.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.haritonenko.librarylendingservice.books.domain.exception.BookAlreadyExistsException;
import ru.haritonenko.librarylendingservice.books.domain.exception.BookNotFoundException;
import ru.haritonenko.librarylendingservice.books.domain.exception.IllegalBookArgumentException;
import ru.haritonenko.librarylendingservice.clients.domain.exception.ClientNotFoundException;
import ru.haritonenko.librarylendingservice.clients.domain.exception.IllegalClientArgumentException;
import ru.haritonenko.librarylendingservice.handler.error.ErrorMessageResponse;
import ru.haritonenko.librarylendingservice.lendings.domain.exception.IllegalLendingArgumentException;
import ru.haritonenko.librarylendingservice.lendings.domain.exception.IllegalLendingStateException;
import ru.haritonenko.librarylendingservice.lendings.domain.exception.LendingNotFoundException;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessageResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String detailedMessage = Stream.concat(
                        ex.getBindingResult().getFieldErrors().stream()
                                .map(error -> error.getField() + ": " + error.getDefaultMessage()),
                        ex.getBindingResult().getGlobalErrors().stream()
                                .map(error -> error.getDefaultMessage())
                )
                .collect(Collectors.joining(", "));

        if (detailedMessage.isEmpty()) {
            detailedMessage = "Request validation failed";
        }

        log.warn("Request validation failed: {}", detailedMessage, ex);
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation error", detailedMessage);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorMessageResponse> handleBindException(BindException ex) {
        String detailedMessage = Stream.concat(
                        ex.getBindingResult().getFieldErrors().stream()
                                .map(error -> error.getField() + ": " + error.getDefaultMessage()),
                        ex.getBindingResult().getGlobalErrors().stream()
                                .map(error -> error.getDefaultMessage())
                )
                .collect(Collectors.joining(", "));

        if (detailedMessage.isEmpty()) {
            detailedMessage = "Request validation failed";
        }

        log.warn("Request binding validation failed: {}", detailedMessage, ex);
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation error", detailedMessage);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorMessageResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        String detailedMessage = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));

        log.warn("Constraint validation failed: {}", detailedMessage, ex);
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation error", detailedMessage);
    }

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ErrorMessageResponse> handleBookNotFoundException(BookNotFoundException ex) {
        log.warn("Book not found: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.NOT_FOUND, "Book not found", ex.getMessage());
    }

    @ExceptionHandler(IllegalBookArgumentException.class)
    public ResponseEntity<ErrorMessageResponse> handleIllegalBookArgumentException(IllegalBookArgumentException ex) {
        log.warn("Invalid book argument: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_REQUEST, "Book argument error", ex.getMessage());
    }

    @ExceptionHandler(BookAlreadyExistsException.class)
    public ResponseEntity<ErrorMessageResponse> handleBookAlreadyExistsException(BookAlreadyExistsException ex) {
        log.warn("Book already exists: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.CONFLICT, "Book already exists", ex.getMessage());
    }

    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<ErrorMessageResponse> handleClientNotFoundException(ClientNotFoundException ex) {
        log.warn("Client not found: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.NOT_FOUND, "Client not found", ex.getMessage());
    }

    @ExceptionHandler(IllegalClientArgumentException.class)
    public ResponseEntity<ErrorMessageResponse> handleIllegalClientArgumentException(IllegalClientArgumentException ex) {
        log.warn("Invalid client argument: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_REQUEST, "Client argument error", ex.getMessage());
    }

    @ExceptionHandler(LendingNotFoundException.class)
    public ResponseEntity<ErrorMessageResponse> handleLendingNotFoundException(LendingNotFoundException ex) {
        log.warn("Lending not found: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.NOT_FOUND, "Lending not found", ex.getMessage());
    }

    @ExceptionHandler(IllegalLendingArgumentException.class)
    public ResponseEntity<ErrorMessageResponse> handleIllegalLendingArgumentException(
            IllegalLendingArgumentException ex
    ) {
        log.warn("Invalid lending argument: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_REQUEST, "Lending argument error", ex.getMessage());
    }

    @ExceptionHandler(IllegalLendingStateException.class)
    public ResponseEntity<ErrorMessageResponse> handleIllegalLendingStateException(IllegalLendingStateException ex) {
        log.warn("Invalid lending state: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.CONFLICT, "Lending state error", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorMessageResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex
    ) {
        log.warn("Request parameter type mismatch: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_REQUEST, "Request parameter type mismatch", ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorMessageResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex
    ) {
        log.warn("Request body parsing failed: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_REQUEST, "Request body parsing error", "Invalid request body format");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorMessageResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Invalid request argument: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_REQUEST, "Request argument error", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorMessageResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorMessageResponse> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.UNAUTHORIZED, "Authentication error", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessageResponse> handleUnexpectedException(Exception ex) {
        log.error("Unexpected error", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "Unexpected error");
    }

    private ResponseEntity<ErrorMessageResponse> buildResponse(
            HttpStatus status,
            String message,
            String detailedMessage
    ) {
        return ResponseEntity
                .status(status)
                .body(new ErrorMessageResponse(message, detailedMessage, LocalDateTime.now().toString()));
    }
}
