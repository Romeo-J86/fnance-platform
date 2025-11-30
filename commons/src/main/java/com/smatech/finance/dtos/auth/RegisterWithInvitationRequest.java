package com.smatech.finance.dtos.auth;

import lombok.Builder;

/**
 * createdBy romeo
 * createdDate 30/11/2025
 * createdTime 09:31
 * projectName Finance Platform
 **/

@Builder
public record RegisterWithInvitationRequest(
        String email,
        String password,  // For invited users, they set password
        String firstName,
        String lastName,
        String invitationToken
) {}

