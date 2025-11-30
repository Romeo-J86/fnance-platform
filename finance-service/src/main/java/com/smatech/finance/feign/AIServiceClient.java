package com.smatech.finance.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 14:54
 * projectName Finance Platform
 **/

@FeignClient(name = "ai-service", path = "/api/ai", configuration = FeignConfig.class)
public interface AIServiceClient {

    @PostMapping("/categorize-transaction")
    ResponseEntity<AICategoryResponse> categorizeTransaction(@RequestBody CategorizeTransactionRequest request);

    @PostMapping("/financial-insights")
    ResponseEntity<FinancialInsightsResponse> getFinancialInsights(@RequestBody FinancialInsightsRequest request);

    @PostMapping("/budget-recommendations")
    ResponseEntity<BudgetRecommendationResponse> getBudgetRecommendations(@RequestBody BudgetRecommendationRequest request);

    // DTO Records
    record CategorizeTransactionRequest(String description, String merchant) {}
    record AICategoryResponse(String category) {}
    record FinancialInsightsRequest(Double totalSpent, Double monthlyBudget, Map<String, Double> spendingByCategory) {}
    record FinancialInsightsResponse(String insights) {}
    record BudgetRecommendationRequest(Map<String, Double> spendingPattern, Map<String, Double> currentBudgets, Double monthlyIncome) {}
    record BudgetRecommendationResponse(String recommendations) {}
}
