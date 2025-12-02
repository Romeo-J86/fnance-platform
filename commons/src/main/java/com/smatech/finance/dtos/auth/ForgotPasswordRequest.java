package com.smatech.finance.dtos.auth;

import lombok.Builder;

/**
 * createdBy romeo
 * createdDate 2/12/2025
 * createdTime 10:04
 * projectName Finance Platform
 **/

@Builder
public record ForgotPasswordRequest(
        String email
) {
}
