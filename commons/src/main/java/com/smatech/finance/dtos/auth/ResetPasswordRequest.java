package com.smatech.finance.dtos.auth;

import lombok.Builder;

/**
 * createdBy romeo
 * createdDate 2/12/2025
 * createdTime 10:00
 * projectName Finance Platform
 **/

@Builder
public record ResetPasswordRequest(
        String token,
        String newPassword,
        String confirmPassword
) {}
