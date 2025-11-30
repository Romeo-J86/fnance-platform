package com.smatech.finance.dtos.auth;


import com.smatech.finance.dtos.auth.enums.UserRole;

import java.util.List;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 12:26
 * projectName Finance Platform
 **/

public record UpdateRolesRequest(List<UserRole> roles) {
}
