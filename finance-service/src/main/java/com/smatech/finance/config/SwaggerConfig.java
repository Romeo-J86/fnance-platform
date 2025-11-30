package com.smatech.finance.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 15:41
 * projectName Finance Platform
 **/

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Digital Banking Core API")
                        .version("1.0")
                        .description("""
                            Core Banking Service API for account management, transactions, and financial operations.
                            
                            **Key Features:**
                            - Account creation and management
                            - Fund transfers between accounts
                            - Transaction history and filtering
                            - Balance inquiries
                            - Loan application processing
                            
                            **Security:** JWT Bearer Token required for all endpoints.
                            """)
                        .contact(new Contact()
                                .name("Banking Platform Team")
                                .email("banking-support@smatech.com")
                                .url("https://banking.smatech.com"))
                        .license(new License()
                                .name("Commercial License")
                                .url("https://smatech.com/license")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token obtained from Auth Service")))
                .addSecurityItem(new SecurityRequirement().addList("apiKey"))
                .components(new Components()
                        .addSecuritySchemes("apiKey", new SecurityScheme()
                                .name("X-API-Key")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("API Key for internal service communication")));
    }
}
