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
 * createdTime 15:43
 * projectName Finance Platform
 **/

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Finance Service")
                        .version("1.0")
                        .description("""
                            AI-Powered Financial Intelligence Service providing smart insights and automation.
                            
                            **AI Capabilities:**
                            - Transaction categorization using Gemini AI
                            - Financial insights and spending analysis
                            - Budget recommendations
                            - Anomaly detection in spending patterns
                            - Credit scoring analysis
                            
                            **Integration:** This service is primarily used internally by other microservices.
                            """)
                        .contact(new Contact()
                                .name("AI & Analytics Team")
                                .email("ai-team@smatech.com")
                                .url("https://ai.smatech.com"))
                        .license(new License()
                                .name("AI Service License")
                                .url("https://smatech.com/ai-license")))
                .addSecurityItem(new SecurityRequirement().addList("serviceAuth"))
                .components(new Components()
                        .addSecuritySchemes("serviceAuth", new SecurityScheme()
                                .name("X-Service-Auth")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("Service-to-service authentication token")))
                .addSecurityItem(new SecurityRequirement().addList("apiKey"))
                .components(new Components()
                        .addSecuritySchemes("apiKey", new SecurityScheme()
                                .name("X-API-Key")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("API Key for internal microservices communication")));
    }
}
