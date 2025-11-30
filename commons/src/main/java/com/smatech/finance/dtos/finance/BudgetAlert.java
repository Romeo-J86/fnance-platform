package com.smatech.finance.dtos.finance;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 15:01
 * projectName Finance Platform
 **/

@Builder
public record BudgetAlert(
        String category,
        BigDecimal budgetedAmount,
        BigDecimal actualSpent,
        BigDecimal percentageUsed
) {}
