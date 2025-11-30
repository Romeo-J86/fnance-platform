package com.smatech.finance.config;

import com.smatech.finance.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 12:18
 * projectName Finance Platform
 **/

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;

    @Override
    public void run(String... args){
        if (userService.findByEmail("admin@finance.com").isEmpty()) {
            userService.registerAdmin(
                    "admin@finance.com",
                    "admin123",
                    "System",
                    "Administrator"
            );
            log.info("Default admin user created: admin@finance.com");
        }

        if (userService.findByEmail("user@finance.com").isEmpty()) {
            userService.registerUser(
                    "user@finance.com",
                    "user123",
                    "Test",
                    "User"
            );
            log.info("Default test user created: user@finance.com");
        }
    }
}
