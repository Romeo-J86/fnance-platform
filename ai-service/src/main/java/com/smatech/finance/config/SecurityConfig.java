package com.smatech.finance.config;

/**
 * createdBy romeo
 * createdDate 30/11/2025
 * createdTime 12:49
 * projectName Finance Platform
 **/

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().permitAll()  // Allow all requests
                )
                .csrf(AbstractHttpConfigurer::disable); // Disable CSRF for API access

        return http.build();
    }
}
