package com.aivle.bookapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {

        Info info = new Info()
                .title("걸어서 서재 속으로")
                .description("도서 관리 시스템")
                .version("v1.0")
                .contact(
                        new Contact()
                                .name("AIVLE AI07 Team")
                                .email("AI07@example.com")
                );

        return new OpenAPI()
                .info(info);
    }
}