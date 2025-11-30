package com.smatech.finance.dtos.finance;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 15:10
 * projectName Finance Platform
 **/

@Builder
public record CreateBudgetRequest(
        String category,
        BigDecimal amount,
        Integer month,
        Integer year
) {}
