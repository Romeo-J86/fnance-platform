package com.smatech.finance.config;

import com.smatech.finance.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import static com.smatech.finance.dtos.auth.enums.UserRole.*;

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

    @Value("${app.system-admin.email}")
    private String systemAdminEmail;
    @Value("${app.system-admin.password}")
    private String systemAdminPassword;
    @Value("${app.test-user.email}")
    private String testUserEmail;
    @Value("${app.test-user.password}")
    private String testUserPassword;

    @Override
    public void run(String... args){
        if (userService.findByEmail(systemAdminEmail).isEmpty()) {
            userService.registerAdmin(
                    systemAdminEmail,
                    systemAdminPassword,
                    "System",
                    "Administrator"
            );
            log.info("Default admin user created: admin@finance.com");
        }

        if (userService.findByEmail(testUserEmail).isEmpty()) {
            userService.registerUser(
                    testUserEmail,
                    testUserPassword,
                    "Test",
                    "User",
                    USER
            );
            log.info("Default test user created: user@finance.com");
        }
    }
}
