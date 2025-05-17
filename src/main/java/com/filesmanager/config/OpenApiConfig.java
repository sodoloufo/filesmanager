package com.filesmanager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI filesManagerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Files Manager API")
                        .description("API pour la gestion des fichiers")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Files Manager Team")
                                .email("contact@filesmanager.com")));
    }
} 