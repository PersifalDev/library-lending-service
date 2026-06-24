package ru.haritonenko.librarylendingservice.lendings.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.haritonenko.librarylendingservice.books.domain.mapper.BookMapper;
import ru.haritonenko.librarylendingservice.clients.domain.mapper.ClientMapper;
import ru.haritonenko.librarylendingservice.lendings.api.dto.ActiveReaderResponseDto;
import ru.haritonenko.librarylendingservice.lendings.api.dto.LendingResponseDto;
import ru.haritonenko.librarylendingservice.lendings.domain.Lending;
import ru.haritonenko.librarylendingservice.lendings.domain.db.entity.LendingEntity;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {BookMapper.class, ClientMapper.class}
)
public interface LendingMapper {

    Lending toDomain(LendingEntity entity);

    LendingResponseDto toResponseDto(Lending lending);

    @Mapping(target = "clientFullName", source = "client.fullName")
    @Mapping(target = "clientBirthDate", source = "client.birthDate")
    @Mapping(target = "bookTitle", source = "book.title")
    @Mapping(target = "bookAuthor", source = "book.author")
    @Mapping(target = "bookIsbn", source = "book.isbn")
    ActiveReaderResponseDto toActiveReaderResponseDto(Lending lending);
}
