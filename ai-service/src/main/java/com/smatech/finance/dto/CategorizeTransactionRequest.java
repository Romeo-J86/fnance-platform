package com.smatech.finance.dto;

import lombok.Builder;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 14:34
 * projectName Finance Platform
 **/

@Builder
public record CategorizeTransactionRequest(
        String description,
        String merchant
) {}
