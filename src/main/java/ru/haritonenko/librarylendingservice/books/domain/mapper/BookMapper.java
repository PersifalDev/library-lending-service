package ru.haritonenko.librarylendingservice.books.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ru.haritonenko.librarylendingservice.books.api.dto.BookResponseDto;
import ru.haritonenko.librarylendingservice.books.domain.Book;
import ru.haritonenko.librarylendingservice.books.domain.db.entity.BookEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BookMapper {

    Book toDomain(BookEntity entity);

    BookResponseDto toResponseDto(Book book);
}
