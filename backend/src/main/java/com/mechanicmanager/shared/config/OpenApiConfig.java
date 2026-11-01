package com.mechanicmanager.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    
    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
    
    @Bean
    public OpenAPI mechanicManagerOpenAPI() {
        return new OpenAPI()
                // Fijo a propósito: springdoc lo deduce del pedido y quedaría "http://localhost"
                // en el contrato versionado. Las rutas ya incluyen /api/v1.
                .servers(List.of(new Server().url("/").description("Mismo servidor que sirve esta documentación")))
                .info(new Info()
                        .title("Mechanic Manager API")
                        .version("1.0.0")
                        .description("API REST de Mechanic Manager, sistema de gestión para talleres mecánicos."))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}