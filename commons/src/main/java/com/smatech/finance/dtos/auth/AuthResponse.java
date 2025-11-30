package com.smatech.finance.dtos.auth;



import com.smatech.finance.dtos.auth.enums.UserRole;

import java.util.List;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 12:11
 * projectName Finance Platform
 **/

public record AuthResponse(
        String token,
        String message,
        String email,
        List<UserRole> roles,
        String firstName,
        String lastName,
        boolean temporaryPassword
) {}
