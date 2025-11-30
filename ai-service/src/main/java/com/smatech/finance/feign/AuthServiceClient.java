package com.smatech.finance.feign;

import com.smatech.finance.dtos.auth.TokenValidationResponse;
import com.smatech.finance.dtos.auth.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * createdBy romeo
 * createdDate 30/11/2025
 * createdTime 15:13
 * projectName Finance Platform
 **/

@FeignClient(name = "auth-service", path = "/api/auth", configuration = FeignConfig.class)
public interface AuthServiceClient {

    @GetMapping("/validate-token")
    TokenValidationResponse validateToken(@RequestHeader("Authorization") String token);

    @GetMapping("/current-user")
    UserResponse getCurrentUser(@RequestHeader("Authorization") String token);
}

