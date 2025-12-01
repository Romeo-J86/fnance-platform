package com.smatech.finance.service.impl;

import com.smatech.finance.domain.Budget;
import com.smatech.finance.domain.Transaction;
import com.smatech.finance.dtos.finance.BudgetAlert;
import com.smatech.finance.dtos.finance.FinancialSummary;
import com.smatech.finance.dtos.finance.UpdateBudgetRequest;
import com.smatech.finance.enums.AlertSeverity;
import com.smatech.finance.enums.Category;
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
import java.util.*;
import java.util.stream.Collectors;

import static com.smatech.finance.enums.Category.OTHER;

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

        if (transaction.getCategory() == null) {
            try {
                Category category = categorizeTransactionWithAI(
                        transaction.getDescription(),
                        transaction.getMerchant()
                );
                transaction.setCategory(category);
                log.info("Auto-categorized transaction as: {}", category);
            } catch (Exception e) {
                log.warn("Failed to auto-categorize transaction, defaulting to OTHER", e);
                transaction.setCategory(OTHER);
            }
        }

        var savedTransaction = transactionRepository.save(transaction);
        var budgets = budgetRepository.findByUserId(userId);
        budgets.stream().filter(budget -> budget.getCategory().equals(transaction.getCategory()))
                .filter(budget -> budget.getMonth().equals(transaction.getTransactionDate().getMonthValue()))
                .filter(budget -> budget.getYear().equals(transaction.getTransactionDate().getYear()))
                .forEach(budget -> budget.setAmount(budget.getAmount().subtract(transaction.getAmount())));
        budgetRepository.saveAll(budgets);
        return savedTransaction;
    }
    private Category categorizeTransactionWithAI(String description, String merchant) {
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

        return OTHER;
    }

    @Override
    public List<Transaction> getTransactions(String userId, Category category, LocalDate startDate,
                                             LocalDate endDate, BigDecimal minAmount, BigDecimal maxAmount) {

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

        return transactionRepository.findTransactionsWithFilters(
                userId, category, startDateTime, endDateTime, minAmount, maxAmount
        );
    }

    @Transactional
    public Budget createBudget(Budget budget, String userId) {
        boolean budgetExist = budgetRepository.existsByCategoryAndMonthAndYear(budget.getCategory(),
                budget.getMonth(), budget.getYear());
        if (budgetExist) {
            throw new RuntimeException("Budget already exists for this category and month");
        }
        budget.setUserId(userId);
        return budgetRepository.save(budget);
    }

    @Override
    public Budget updateBudget(Long id, UpdateBudgetRequest updateBudgetRequest, String userId) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        budget.setAmount(updateBudgetRequest.amount());
        budget.setMonth(updateBudgetRequest.month());
        budget.setYear(updateBudgetRequest.year());
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

        YearMonth yearMonth = YearMonth.of(currentYear, currentMonth);
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Transaction> monthlyTransactions =
                transactionRepository.findByUserIdAndTransactionDateBetween(userId, startOfMonth, endOfMonth);

        BigDecimal totalSpent = monthlyTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Budget> monthlyBudgets = budgetRepository.findByUserIdAndMonthAndYear(userId, currentMonth, currentYear);
        BigDecimal monthlyBudget = monthlyBudgets.stream()
                .map(Budget::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Category, BigDecimal> spendingByCategory = new HashMap<>();
        for (Transaction transaction : monthlyTransactions) {
            Category category = transaction.getCategory();
            BigDecimal amount = transaction.getAmount();
            spendingByCategory.merge(category, amount, BigDecimal::add);
        }

        List<BudgetAlert> budgetAlerts = calculateBudgetAlerts(monthlyBudgets, spendingByCategory);

        BigDecimal budgetRemaining = monthlyBudget.subtract(totalSpent).max(BigDecimal.ZERO);
        BigDecimal budgetUtilization = monthlyBudget.compareTo(BigDecimal.ZERO) > 0 ?
                totalSpent.divide(monthlyBudget, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100")) :
                BigDecimal.ZERO;

        Map<AlertSeverity, Long> alertCounts = budgetAlerts.stream()
                .collect(Collectors.groupingBy(
                        BudgetAlert::alertSeverity,
                        Collectors.counting()
                ));

        Map<String, Long> alertSummary = new HashMap<>();
        alertSummary.put("CRITICAL", alertCounts.getOrDefault(AlertSeverity.CRITICAL, 0L));
        alertSummary.put("HIGH", alertCounts.getOrDefault(AlertSeverity.HIGH, 0L));
        alertSummary.put("MEDIUM", alertCounts.getOrDefault(AlertSeverity.MEDIUM, 0L));
        alertSummary.put("LOW", alertCounts.getOrDefault(AlertSeverity.LOW, 0L));
        alertSummary.put("INFO", alertCounts.getOrDefault(AlertSeverity.INFO, 0L));
        alertSummary.put("TOTAL", (long) budgetAlerts.size());

        long priorityAlerts = alertSummary.get("CRITICAL") + alertSummary.get("HIGH");
        alertSummary.put("PRIORITY_ALERTS", priorityAlerts);

        return FinancialSummary.builder()
                .totalSpent(totalSpent)
                .monthlyBudget(monthlyBudget)
                .budgetRemaining(budgetRemaining)
                .budgetUtilization(budgetUtilization.setScale(2, RoundingMode.HALF_UP))
                .spendingByCategory(spendingByCategory)
                .budgetAlerts(budgetAlerts)
                .alertSummary(alertSummary)
                .month(currentMonth)
                .year(currentYear)
                .generatedAt(LocalDateTime.now())
                .build();
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
    public String getBudgetRecommendations(String userId, BigDecimal monthlyIncome) {
        try {
            FinancialSummary summary = getFinancialSummary(userId);
            List<Budget> budgets = getBudgets(userId);

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

    private List<BudgetAlert> calculateBudgetAlerts(List<Budget> budgets, Map<Category, BigDecimal> spendingByCategory) {
        List<BudgetAlert> alerts = new ArrayList<>();

        for (Budget budget : budgets) {
            Category category = budget.getCategory();
            BigDecimal budgetAmount = budget.getAmount();
            BigDecimal spent = spendingByCategory.getOrDefault(category, BigDecimal.ZERO);

            if (budgetAmount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal percentageUsed = spent.divide(budgetAmount, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));

                AlertSeverity severity = AlertSeverity.fromPercentage(percentageUsed);

                if (severity != null) {
                    String message = generateAlertMessage(category, spent, budgetAmount, percentageUsed, severity);
                    String recommendation = generateRecommendation(category, severity, percentageUsed);

                    BudgetAlert alert = BudgetAlert.builder()
                            .category(category)
                            .budgetedAmount(budgetAmount)
                            .actualSpent(spent)
                            .percentageUsed(percentageUsed.setScale(2, RoundingMode.HALF_UP))
                            .alertSeverity(severity)
                            .message(message)
                            .recommendation(recommendation)
                            .alertTime(LocalDateTime.now())
                            .build();

                    alerts.add(alert);
                }
            }
        }

        for (Map.Entry<Category, BigDecimal> entry : spendingByCategory.entrySet()) {
            Category category = entry.getKey();
            BigDecimal spent = entry.getValue();

            if (spent.compareTo(new BigDecimal("50")) > 0) {
                boolean hasBudget = budgets.stream()
                        .anyMatch(b -> b.getCategory() == category);

                if (!hasBudget) {
                    String message = String.format("[INFO] %s SPENDING DETECTED: You've spent $%.2f without a budget set. " +
                            "Consider creating a budget for better tracking.", category, spent);
                    String recommendation = "Set a monthly budget for this category based on your average spending pattern.";

                    BudgetAlert alert = BudgetAlert.builder()
                            .category(category)
                            .budgetedAmount(BigDecimal.ZERO)
                            .actualSpent(spent)
                            .percentageUsed(new BigDecimal("0"))
                            .alertSeverity(AlertSeverity.INFO)
                            .message(message)
                            .recommendation(recommendation)
                            .alertTime(LocalDateTime.now())
                            .build();

                    alerts.add(alert);
                }
            }
        }

        alerts.sort(Comparator.comparingInt(alert -> -alert.alertSeverity().getPriority()));

        return alerts;
    }

    private String generateRecommendation(Category category, AlertSeverity severity, BigDecimal percentage) {
        String baseRecommendation = switch (severity) {
            case CRITICAL -> "IMMEDIATE ACTION REQUIRED: ";
            case HIGH -> "URGENT REVIEW RECOMMENDED: ";
            case MEDIUM -> "CONSIDER ADJUSTING: ";
            case LOW -> "SUGGESTION: ";
            case INFO -> "RECOMMENDATION: ";
        };

        String categorySpecific = switch (category) {
            case RENT -> {
                if (severity == AlertSeverity.CRITICAL) {
                    yield "Review housing options or consider negotiating rent terms.";
                } else if (severity == AlertSeverity.HIGH) {
                    yield "Monitor rent payments and consider downsizing if trend continues.";
                } else {
                    yield "Ensure rent remains within 30% of monthly income.";
                }
            }

            case FOOD -> {
                if (severity == AlertSeverity.CRITICAL || severity == AlertSeverity.HIGH) {
                    yield "Implement meal planning and reduce dining out frequency.";
                } else {
                    yield "Continue tracking food expenses to maintain healthy spending habits.";
                }
            }

            case GROCERIES -> {
                if (severity == AlertSeverity.CRITICAL) {
                    yield "Create shopping lists with strict budgets and avoid impulse purchases.";
                } else if (severity == AlertSeverity.HIGH) {
                    yield "Consider buying in bulk for non-perishables and using discount stores.";
                } else {
                    yield "Maintain grocery spending within allocated budget.";
                }
            }

            case TRANSPORT -> {
                if (severity == AlertSeverity.CRITICAL) {
                    yield "Switch to public transportation or carpooling immediately.";
                } else if (severity == AlertSeverity.HIGH) {
                    yield "Combine trips and reduce non-essential travel.";
                } else {
                    yield "Monitor fuel consumption and maintenance costs.";
                }
            }

            case UTILITIES -> {
                if (severity == AlertSeverity.CRITICAL) {
                    yield "Implement energy-saving measures and review service providers.";
                } else if (severity == AlertSeverity.HIGH) {
                    yield "Monitor usage patterns and consider off-peak consumption.";
                } else {
                    yield "Regularly review utility bills for efficiency improvements.";
                }
            }

            case ENTERTAINMENT -> {
                if (severity == AlertSeverity.CRITICAL) {
                    yield "Cancel non-essential subscriptions and find free alternatives.";
                } else if (severity == AlertSeverity.HIGH) {
                    yield "Limit entertainment spending to essential services only.";
                } else {
                    yield "Balance entertainment expenses with other financial priorities.";
                }
            }

            case SHOPPING -> {
                if (severity == AlertSeverity.CRITICAL) {
                    yield "Implement 30-day waiting period for non-essential purchases.";
                } else if (severity == AlertSeverity.HIGH) {
                    yield "Create needs vs wants list before making purchases.";
                } else {
                    yield "Track shopping expenses to avoid impulse buying.";
                }
            }

            case OTHER -> {
                if (severity == AlertSeverity.CRITICAL) {
                    yield "Review all miscellaneous expenses and eliminate non-essentials.";
                } else {
                    yield "Categorize miscellaneous spending for better tracking.";
                }
            }
        };

        String percentageAdvice = switch (severity) {
            case CRITICAL -> String.format(" Current spending at %.1f%% exceeds budget limit.", percentage);
            case HIGH -> String.format(" At %.1f%% of budget, immediate review is advised.", percentage);
            case MEDIUM -> String.format(" Current utilization is %.1f%%.", percentage);
            case LOW -> String.format(" Utilization at %.1f%% is within acceptable range.", percentage);
            case INFO -> " Consider establishing a budget for future planning.";
        };

        return baseRecommendation + categorySpecific + percentageAdvice;
    }

    private String generateAlertMessage(Category category, BigDecimal spent,
                                        BigDecimal budget, BigDecimal percentage,
                                        AlertSeverity severity) {

        String formattedPercentage = percentage.setScale(1, RoundingMode.HALF_UP).toString();
        String formattedSpent = String.format("$%.2f", spent);
        String formattedBudget = String.format("$%.2f", budget);

        return switch (severity) {
            case CRITICAL -> {
                BigDecimal overAmount = spent.subtract(budget);
                String formattedOver = String.format("$%.2f", overAmount);
                yield String.format("[%s] %s BUDGET EXCEEDED: You have spent %s which is %s over " +
                                "your %s budget (%s%% utilized). %s",
                        severity.getLevel(), category, formattedSpent, formattedOver,
                        formattedBudget, formattedPercentage, severity.getShortDescription());
            }

            case HIGH -> String.format("[%s] %s BUDGET WARNING: You have used %s%% of your %s budget " +
                            "(%s spent). %s",
                    severity.getLevel(), category, formattedPercentage, formattedBudget,
                    formattedSpent, severity.getShortDescription());

            case MEDIUM -> String.format("[%s] %s SPENDING NOTICE: %s%% of budget utilized " +
                            "(%s spent of %s budget). %s",
                    severity.getLevel(), category, formattedPercentage, formattedSpent,
                    formattedBudget, severity.getShortDescription());

            case LOW -> String.format("[%s] %s SPENDING UPDATE: %s%% of budget utilized. %s",
                    severity.getLevel(), category, formattedPercentage, severity.getShortDescription());

            case INFO -> String.format("[%s] %s SPENDING DETECTED: %s spent without a defined budget. %s",
                    severity.getLevel(), category, formattedSpent, severity.getShortDescription());
        };
    }
}
