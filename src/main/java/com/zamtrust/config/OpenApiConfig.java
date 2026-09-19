package com.zamtrust.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_KEY = "bearerAuth";

    @Bean
    public OpenAPI zamtrustOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ZamTrust API")
                        .description("Secure Digital Document Signing and Verification Platform. "
                                + "Provides document upload, cryptographic hashing (SHA-256), "
                                + "RSA-2048 digital signing, verification, QR codes, and audit trails.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ZamTrust Project")
                                .email("support@zamtrust.example"))
                        .license(new License()
                                .name("Academic Use — MSc Computer Science")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development")
                ))
                .components(new Components()
                        .addSecuritySchemes(BEARER_KEY, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste your JWT token here. Obtain it via POST /api/auth/login.")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_KEY));
    }
}
