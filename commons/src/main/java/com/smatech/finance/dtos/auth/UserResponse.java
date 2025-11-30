package com.smatech.finance.dtos.auth;

import com.smatech.finance.dtos.auth.enums.UserRole;
import lombok.Builder;

import java.util.List;

/**
 * createdBy romeo
 * createdDate 30/11/2025
 * createdTime 12:26
 * projectName Finance Platform
 **/

@Builder
public record UserResponse(String email, String firstName, String lastName, List<UserRole> roles) {}
