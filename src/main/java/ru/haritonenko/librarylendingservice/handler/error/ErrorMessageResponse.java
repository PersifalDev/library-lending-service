package ru.haritonenko.librarylendingservice.handler.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorMessageResponse {

    private final String message;
    private final String detailedMessage;
    private final String timestamp;
}
