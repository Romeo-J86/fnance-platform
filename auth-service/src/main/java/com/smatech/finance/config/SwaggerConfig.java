package com.smatech.finance.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 15:42
 * projectName Finance Platform
 **/

@Configuration
public class SwaggerConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Authentication & Authorization API")
                        .version("1.0")
                        .description("""
                            Identity and Access Management Service for the Digital Banking Platform.
                            
                            **Features:**
                            - User registration and authentication
                            - JWT token generation and validation
                            - Role-based access control (RBAC)
                            - User profile management
                            - Invitation-based user onboarding
                            - Admin user management
                            
                            **Note:** Some endpoints require ADMIN privileges.
                            """)
                        .contact(new Contact()
                                .name("Security & IAM Team")
                                .email("security@smatech.com")
                                .url("https://security.smatech.com"))
                        .license(new License()
                                .name("Security License")
                                .url("https://smatech.com/security-license")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token for authenticated requests")))
                .externalDocs(new io.swagger.v3.oas.models.ExternalDocumentation()
                        .description("Authentication Guide")
                        .url("https://docs.smatech.com/auth-guide"));
    }
}
