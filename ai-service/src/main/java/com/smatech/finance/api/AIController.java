package com.smatech.finance.api;

import com.smatech.finance.dto.*;
import com.smatech.finance.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 14:31
 * projectName Finance Platform
 **/

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final GeminiService geminiService;

    @PostMapping("/categorize-transaction")
    public ResponseEntity<AICategoryResponse> categorizeTransaction(@RequestBody CategorizeTransactionRequest request) {
        log.info("Received categorization request for transaction: {} - {}",
                request.merchant(), request.description());

        String category = geminiService.categorizeTransaction(
                request.description(),
                request.merchant()
        );

        return ResponseEntity.ok(new AICategoryResponse(category));
    }

    @PostMapping("/financial-insights")
    public ResponseEntity<FinancialInsightsResponse> getFinancialInsights(@RequestBody FinancialInsightsRequest request) {
        log.info("Generating financial insights for user spending data");

        String insights = geminiService.generateFinancialInsights(
                request.totalSpent(),
                request.monthlyBudget(),
                request.spendingByCategory().toString()
        );

        return ResponseEntity.ok(new FinancialInsightsResponse(insights));
    }

    @PostMapping("/budget-recommendations")
    public ResponseEntity<BudgetRecommendationResponse> getBudgetRecommendations(@RequestBody BudgetRecommendationRequest request) {
        log.info("Generating budget recommendations");

        String recommendations = geminiService.generateBudgetRecommendations(
                request.spendingPattern().toString(),
                request.currentBudgets().toString(),
                request.monthlyIncome()
        );

        return ResponseEntity.ok(new BudgetRecommendationResponse(recommendations));
    }
}
