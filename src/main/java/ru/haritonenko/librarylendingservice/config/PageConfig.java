package ru.haritonenko.librarylendingservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.haritonenko.librarylendingservice.config.properties.PageProperties;

@Slf4j
@Component
@RequiredArgsConstructor
public class PageConfig {

    private final PageProperties pageProperties;

    public Pageable pageable(Integer pageNumber, Integer pageSize) {
        int number = pageNumber == null ? pageProperties.getDefaultPageNumber() : pageNumber;
        int size = pageSize == null ? pageProperties.getDefaultPageSize() : pageSize;
        if (number < 0) {
            log.warn("Page number is negative: {}", number);
            throw new IllegalArgumentException("Page number must be zero or positive");
        }
        if (size < 1) {
            log.warn("Page size is less than one: {}", size);
            throw new IllegalArgumentException("Page size must be positive");
        }
        if (size > pageProperties.getMaxPageSize()) {
            log.warn("Page size={} exceeds max page size={}", size, pageProperties.getMaxPageSize());
            throw new IllegalArgumentException(
                    String.format("Page size must be less than or equal to %d", pageProperties.getMaxPageSize())
            );
        }
        return PageRequest.of(number, size);
    }
}
