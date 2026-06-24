package ru.haritonenko.librarylendingservice.clients.api.dto.filter;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Getter
@Setter
public class ClientPageFilter {

    @Min(value = 0, message = "Min page number value is 0")
    private Integer pageNumber;

    @Min(value = 1, message = "Min page size value is 1")
    @Max(value = 100, message = "Max page size value is 100")
    private Integer pageSize;
}
