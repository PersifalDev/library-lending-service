package ru.haritonenko.librarylendingservice.clients.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ru.haritonenko.librarylendingservice.clients.api.dto.ClientResponseDto;
import ru.haritonenko.librarylendingservice.clients.domain.Client;
import ru.haritonenko.librarylendingservice.clients.domain.db.entity.ClientEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ClientMapper {

    Client toDomain(ClientEntity entity);

    ClientResponseDto toResponseDto(Client client);
}
