package ru.haritonenko.librarylendingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LibraryLendingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryLendingServiceApplication.class, args);
    }

}
