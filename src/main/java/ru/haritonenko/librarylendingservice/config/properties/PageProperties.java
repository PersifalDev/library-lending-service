package ru.haritonenko.librarylendingservice.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.pages")
public class PageProperties {

    private int defaultPageNumber;
    private int defaultPageSize;
    private int maxPageSize;
}
