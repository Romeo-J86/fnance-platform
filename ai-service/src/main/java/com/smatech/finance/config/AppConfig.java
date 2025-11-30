package com.smatech.finance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 14:41
 * projectName Finance Platform
 **/

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
