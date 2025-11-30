package com.smatech.finance.dtos.auth;

import com.smatech.finance.dtos.auth.enums.UserRole;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * createdBy romeo
 * createdDate 30/11/2025
 * createdTime 11:15
 * projectName Finance Platform
 **/

@Builder
public record InvitationResponse(
        String token,
        String email,
        UserRole role,
        LocalDateTime expiresAt
) {}
