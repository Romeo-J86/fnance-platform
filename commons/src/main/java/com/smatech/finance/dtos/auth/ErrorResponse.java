package com.smatech.finance.dtos.auth;

import lombok.Data;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 11:46
 * projectName Finance Platform
 **/

@Data
public class ErrorResponse {
    private String error;
    public ErrorResponse(String error) {
        this.error = error;
    }
}
