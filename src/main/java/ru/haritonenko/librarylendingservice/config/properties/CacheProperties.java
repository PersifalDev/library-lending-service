package ru.haritonenko.librarylendingservice.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.cache")
public class CacheProperties {

    private Duration booksTtl;
    private Duration clientsTtl;
    private Duration lendingsTtl;
    private String bookKeyPrefix;
    private String clientKeyPrefix;
    private String lendingKeyPrefix;
    private String lendingByClientKeyPrefix;
    private String lendingByBookKeyPrefix;
}
