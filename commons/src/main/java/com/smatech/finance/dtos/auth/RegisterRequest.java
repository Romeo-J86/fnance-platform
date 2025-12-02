package com.smatech.finance.dtos.auth;

import com.smatech.finance.dtos.auth.enums.UserRole;
import lombok.Builder;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 11:39
 * projectName Finance Platform
 **/

@Builder
public record RegisterRequest(
        String email,
        String firstName,
        String lastName,
        UserRole role
) {}
