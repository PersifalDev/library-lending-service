package ru.haritonenko.librarylendingservice.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.search")
public class SearchProperties {

    private String emptyFilterValue;
}
