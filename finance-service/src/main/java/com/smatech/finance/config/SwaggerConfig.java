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
 * createdTime 15:43
 * projectName Finance Platform
 **/

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Finance Service API")
                        .version("1.0")
                        .description("""
                            Core Financial Service responsible for managing financial records and operations.
                            
                            **Core Capabilities:**
                            - **Budget Management:** Create, update, and monitor budgets.
                            - **Transaction Management:** Record and retrieve financial transactions.
                            - **Financial Summaries:** Aggregation of financial data and reporting.
                            
                            **Integration:** Serves as the central ledger for the Finance Platform.
                            """)
                        .contact(new Contact()
                                .name("Finance Engineering Team")
                                .email("finance-team@smatech.com")
                                .url("https://finance.smatech.com"))
                        .license(new License()
                                .name("Proprietary License")
                                .url("https://smatech.com/license")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your JWT token")));
    }
}
