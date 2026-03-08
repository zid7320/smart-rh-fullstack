package com.smart.rh.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "Bearer Authentication";

    @Bean
    public OpenAPI smartRhOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SMART RH 4.0 API")
                        .description("""
                                Intelligent HR Information System — SMART RH 4.0.

                                Provides secure REST APIs for employee management, recruitment,
                                payroll, leave management, evaluations, trainings, and
                                AI-powered face-recognition attendance (IoT + MQTT ready).
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("GOOD GOV IT")
                                .email("contact@goodgovit.ma"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://goodgovit.ma")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME,
                                new SecurityScheme()
                                        .name(BEARER_SCHEME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Provide a valid JWT token. Obtain one via POST /api/auth/login")));
    }
}
