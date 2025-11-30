package com.smatech.finance.api;

import com.smatech.finance.domain.Budget;
import com.smatech.finance.domain.Transaction;
import com.smatech.finance.dtos.finance.*;
import com.smatech.finance.service.FinanceService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 15:08
 * projectName Finance Platform
 **/

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Finance Management", description = "APIs for managing financial transactions and budgets")
public class FinanceController {

    private final FinanceService financeService;
    private final ModelMapper modelMapper;

    @PostMapping("/transactions")
    public ResponseEntity<TransactionDTO> createTransaction(@RequestBody CreateTransactionRequest request,
                                                            @RequestHeader("X-User-Id") String userId) {
        Transaction transaction = convertToTransaction(request);
        Transaction savedTransaction = financeService.createTransaction(transaction, userId);
        return ResponseEntity.ok(modelMapper.map(savedTransaction, TransactionDTO.class));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDTO>> getTransactions(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount) {

        List<Transaction> transactions = financeService.getTransactions(
                userId, category, startDate, endDate, minAmount, maxAmount
        );
        return ResponseEntity.ok(transactions.stream()
                .map(transaction -> modelMapper.map(transaction, TransactionDTO.class))
                .toList());
    }

    @PostMapping("/budgets")
    public ResponseEntity<BudgetDTO> createBudget(@RequestBody CreateBudgetRequest request,
                                                  @RequestHeader("X-User-Id") String userId) {
        Budget budget = convertToBudget(request);
        Budget savedBudget = financeService.createBudget(budget, userId);
        return ResponseEntity.ok(modelMapper.map(savedBudget, BudgetDTO.class));
    }

    @GetMapping("/budgets")
    public ResponseEntity<List<BudgetDTO>> getBudgets(@RequestHeader("X-User-Id") String userId) {
        List<Budget> budgets = financeService.getBudgets(userId);
        return ResponseEntity.ok(budgets.stream()
                .map(budget -> modelMapper.map(budget, BudgetDTO.class))
                .toList());
    }

    @GetMapping("/summary")
    public ResponseEntity<FinancialSummary> getFinancialSummary(@RequestHeader("X-User-Id") String userId) {
        FinancialSummary summary = financeService.getFinancialSummary(userId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/ai-insights")
    public ResponseEntity<String> getAIFinancialInsights(@RequestHeader("X-User-Id") String userId) {
        String insights = financeService.getFinancialInsights(userId);
        return ResponseEntity.ok(insights);
    }

    @GetMapping("/ai-budget-recommendations")
    public ResponseEntity<String> getAIBudgetRecommendations(@RequestHeader("X-User-Id") String userId) {
        String recommendations = financeService.getBudgetRecommendations(userId);
        return ResponseEntity.ok(recommendations);
    }

    private Transaction convertToTransaction(CreateTransactionRequest request) {
        Transaction transaction = new Transaction();
        transaction.setAmount(request.amount());
        transaction.setMerchant(request.merchant());
        transaction.setDescription(request.description());
        transaction.setCategory(request.category());
        transaction.setTransactionDate(request.transactionDate());
        return transaction;
    }

    private Budget convertToBudget(CreateBudgetRequest request) {
        Budget budget = new Budget();
        budget.setCategory(request.category());
        budget.setAmount(request.amount());
        budget.setMonth(request.month());
        budget.setYear(request.year());
        return budget;
    }
}
