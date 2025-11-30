package com.smatech.finance.service.impl;

import com.smatech.finance.domain.Budget;
import com.smatech.finance.domain.Transaction;
import com.smatech.finance.dtos.finance.BudgetAlert;
import com.smatech.finance.dtos.finance.FinancialSummary;
import com.smatech.finance.feign.AIServiceClient;
import com.smatech.finance.persistence.BudgetRepository;
import com.smatech.finance.persistence.TransactionRepository;
import com.smatech.finance.service.FinanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 15:02
 * projectName Finance Platform
 **/

@Slf4j
@Service
@RequiredArgsConstructor
public class FinanceServiceImpl implements FinanceService {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final AIServiceClient aiServiceClient;

    @Transactional
    public Transaction createTransaction(Transaction transaction, String userId) {
        transaction.setUserId(userId);

        // Auto-categorize using AI if category is not provided
        if (transaction.getCategory() == null || transaction.getCategory().isEmpty()) {
            try {
                String category = categorizeTransactionWithAI(
                        transaction.getDescription(),
                        transaction.getMerchant()
                );
                transaction.setCategory(category);
                log.info("Auto-categorized transaction as: {}", category);
            } catch (Exception e) {
                log.warn("Failed to auto-categorize transaction, defaulting to OTHER", e);
                transaction.setCategory("OTHER");
            }
        }

        return transactionRepository.save(transaction);
    }
    private String categorizeTransactionWithAI(String description, String merchant) {
        try {
            AIServiceClient.CategorizeTransactionRequest request =
                    new AIServiceClient.CategorizeTransactionRequest(description, merchant);

            ResponseEntity<AIServiceClient.AICategoryResponse> response =
                    aiServiceClient.categorizeTransaction(request);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().category();
            }
        } catch (Exception e) {
            log.error("Failed to categorize transaction with AI", e);
        }

        return "OTHER";
    }

    @Override
    public List<Transaction> getTransactions(String userId, String category, LocalDate startDate,
                                             LocalDate endDate, BigDecimal minAmount, BigDecimal maxAmount) {

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

        return transactionRepository.findTransactionsWithFilters(
                userId, category, startDateTime, endDateTime, minAmount, maxAmount
        );
    }

    @Transactional
    public Budget createBudget(Budget budget, String userId) {
        budget.setUserId(userId);
        return budgetRepository.save(budget);
    }

    @Override
    public List<Budget> getBudgets(String userId) {
        return budgetRepository.findByUserId(userId);
    }

    @Override
    public FinancialSummary getFinancialSummary(String userId) {
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        // Get current month's transactions
        YearMonth yearMonth = YearMonth.of(currentYear, currentMonth);
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Transaction> monthlyTransactions =
                transactionRepository.findByUserIdAndTransactionDateBetween(userId, startOfMonth, endOfMonth);

        // Calculate total spent
        BigDecimal totalSpent = monthlyTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get current month's budgets
        List<Budget> monthlyBudgets = budgetRepository.findByUserIdAndMonthAndYear(userId, currentMonth, currentYear);
        BigDecimal monthlyBudget = monthlyBudgets.stream()
                .map(Budget::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate spending by category
        Map<String, BigDecimal> spendingByCategory = new HashMap<>();
        for (Transaction transaction : monthlyTransactions) {
            String category = transaction.getCategory();
            BigDecimal amount = transaction.getAmount();
            spendingByCategory.merge(category, amount, BigDecimal::add);
        }

        // Calculate budget alerts
        List<BudgetAlert> budgetAlerts = calculateBudgetAlerts(monthlyBudgets, spendingByCategory);

        return new FinancialSummary(totalSpent, monthlyBudget, spendingByCategory, budgetAlerts);
    }

    @Override
    public String getFinancialInsights(String userId) {
        try {
            FinancialSummary summary = getFinancialSummary(userId);

            AIServiceClient.FinancialInsightsRequest request =
                    new AIServiceClient.FinancialInsightsRequest(
                            summary.totalSpent().doubleValue(),
                            summary.monthlyBudget().doubleValue(),
                            summary.spendingByCategory().entrySet().stream()
                                    .collect(Collectors.toMap(
                                            Map.Entry::getKey,
                                            e -> e.getValue().doubleValue()
                                    ))
                    );

            ResponseEntity<AIServiceClient.FinancialInsightsResponse> response =
                    aiServiceClient.getFinancialInsights(request);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().insights();
            }
        } catch (Exception e) {
            log.error("Failed to get financial insights from AI service", e);
        }

        return "Unable to generate insights at this time. Please try again later.";
    }

    @Override
    public String getBudgetRecommendations(String userId) {
        try {
            FinancialSummary summary = getFinancialSummary(userId);
            List<Budget> budgets = getBudgets(userId);

            // Assume monthly income (in real app, this would come from user profile)
            BigDecimal monthlyIncome = new BigDecimal("3000.00");

            AIServiceClient.BudgetRecommendationRequest request =
                    new AIServiceClient.BudgetRecommendationRequest(
                            summary.spendingByCategory().entrySet().stream()
                                    .collect(Collectors.toMap(
                                            Map.Entry::getKey,
                                            e -> e.getValue().doubleValue()
                                    )),
                            budgets.stream()
                                    .collect(Collectors.toMap(
                                            Budget::getCategory,
                                            budget -> budget.getAmount().doubleValue()
                                    )),
                            monthlyIncome.doubleValue()
                    );

            ResponseEntity<AIServiceClient.BudgetRecommendationResponse> response =
                    aiServiceClient.getBudgetRecommendations(request);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().recommendations();
            }
        } catch (Exception e) {
            log.error("Failed to get budget recommendations from AI service", e);
        }

        return "Unable to generate budget recommendations at this time. Please try again later.";
    }

    private List<BudgetAlert> calculateBudgetAlerts(List<Budget> budgets, Map<String, BigDecimal> spendingByCategory) {
        return budgets.stream()
                .map(budget -> {
                    BigDecimal spent = spendingByCategory.getOrDefault(budget.getCategory(), BigDecimal.ZERO);
                    BigDecimal percentageUsed = budget.getAmount().compareTo(BigDecimal.ZERO) > 0 ?
                            spent.divide(budget.getAmount(), 2, RoundingMode.HALF_UP).multiply(new BigDecimal("100")) :
                            BigDecimal.ZERO;

                    return new BudgetAlert(budget.getCategory(), budget.getAmount(), spent, percentageUsed);
                })
                .filter(alert -> alert.percentageUsed().compareTo(new BigDecimal("80")) > 0) // Only alerts for >80% usage
                .collect(Collectors.toList());
    }
}
