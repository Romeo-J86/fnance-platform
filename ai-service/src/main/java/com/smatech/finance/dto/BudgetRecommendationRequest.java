package com.smatech.finance.dto;

import lombok.Builder;

import java.util.Map;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 14:38
 * projectName Finance Platform
 **/

@Builder
public record BudgetRecommendationRequest(
        Map<String, Double> spendingPattern,
        Map<String, Double> currentBudgets,
        Double monthlyIncome
) {}
