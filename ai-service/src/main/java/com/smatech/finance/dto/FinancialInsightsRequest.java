package com.smatech.finance.dto;

import lombok.Builder;

import java.util.Map;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 14:36
 * projectName Finance Platform
 **/

@Builder
public record FinancialInsightsRequest(
        Double totalSpent,
        Double monthlyBudget,
        Map<String, Double>spendingByCategory
) {}
