package com.smatech.finance.dtos.finance;

import com.smatech.finance.enums.Category;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
        // Core metrics
        BigDecimal totalSpent,
        BigDecimal monthlyBudget,
        BigDecimal budgetRemaining,
        BigDecimal budgetUtilization,

        // Detailed breakdown
        Map<Category, BigDecimal> spendingByCategory,

        // Alerts
        List<BudgetAlert> budgetAlerts,
        Map<String, Long> alertSummary, // Count of alerts by severity

        // Metadata
        Integer month,
        Integer year,
        LocalDateTime generatedAt,
        String currency
) {
    public FinancialSummary {
        if (currency == null) {
            currency = "USD";
        }
        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
    }
}