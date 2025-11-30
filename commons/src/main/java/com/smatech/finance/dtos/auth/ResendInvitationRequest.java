package com.smatech.finance.dtos.auth;

import com.smatech.finance.dtos.auth.enums.UserRole;
import lombok.Builder;

/**
 * createdBy romeo
 * createdDate 30/11/2025
 * createdTime 11:14
 * projectName Finance Platform
 **/

@Builder
public record ResendInvitationRequest(String email, UserRole role){}
