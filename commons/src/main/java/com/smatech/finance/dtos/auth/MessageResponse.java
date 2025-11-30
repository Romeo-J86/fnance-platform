package com.smatech.finance.dtos.auth;

import lombok.Data;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 11:44
 * projectName Finance Platform
 **/

@Data
public class MessageResponse {
    private String message;
    public MessageResponse(String message) {
        this.message = message;
    }
}
