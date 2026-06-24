package ru.haritonenko.librarylendingservice.config.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.haritonenko.librarylendingservice.config.properties.OpenApiProperties;

import java.util.Collections;

@Configuration
@RequiredArgsConstructor
public class OpenApiConfiguration {

    private final OpenApiProperties openApiProperties;

    @Bean
    public OpenAPI libraryLendingOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Library Lending Service API")
                        .version("1.0.0")
                        .description("REST API для заказа и выдачи книг в библиотеке"))
                .components(new Components()
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ))
                .servers(Collections.singletonList(new Server().url(openApiProperties.getServerUrl())));
    }
}
