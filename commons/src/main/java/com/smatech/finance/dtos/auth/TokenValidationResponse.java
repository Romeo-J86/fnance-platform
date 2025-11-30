package com.smatech.finance.dtos.auth;

import lombok.Builder;

/**
 * createdBy romeo
 * createdDate 30/11/2025
 * createdTime 12:26
 * projectName Finance Platform
 **/

@Builder
public record TokenValidationResponse(boolean valid, String email, String message) {}
