package com.smatech.finance.dtos.finance;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 14:59
 * projectName Finance Platform
 **/

@Builder
public record FinancialSummary(
        BigDecimal totalSpent,
        BigDecimal monthlyBudget,
        Map<String, BigDecimal>spendingByCategory,
        List<BudgetAlert>budgetAlerts
) {}
