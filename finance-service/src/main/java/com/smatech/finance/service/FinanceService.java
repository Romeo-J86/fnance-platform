package com.smatech.finance.service;

import com.smatech.finance.domain.Budget;
import com.smatech.finance.domain.Transaction;
import com.smatech.finance.dtos.finance.FinancialSummary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 14:56
 * projectName Finance Platform
 **/

public interface FinanceService {
    Transaction createTransaction(Transaction transaction, String userId);
    List<Transaction> getTransactions(String userId, String category, LocalDate startDate,
                                      LocalDate endDate, BigDecimal minAmount, BigDecimal maxAmount);
    Budget createBudget(Budget budget, String userId);
    List<Budget> getBudgets(String userId);
    FinancialSummary getFinancialSummary(String userId);
    String getFinancialInsights(String userId);
    String getBudgetRecommendations(String userId);
}
