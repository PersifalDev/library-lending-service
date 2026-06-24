package ru.haritonenko.librarylendingservice.books.api.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class BookUpdateRequestDto {

    @NotBlank(message = "Book title can not be blank")
    @Size(max = 255, message = "Max title size is 255")
    private String title;

    @NotBlank(message = "Book author can not be blank")
    @Size(max = 255, message = "Max author size is 255")
    private String author;

    @NotBlank(message = "Book isbn can not be blank")
    @Size(max = 32, message = "Max isbn size is 32")
    private String isbn;
}
