package com.smatech.finance.dtos.auth;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 11:41
 * projectName Finance Platform
 **/

public record LoginRequest(
        String email,
        String password
) {}
