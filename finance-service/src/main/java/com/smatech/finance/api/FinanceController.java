package com.smatech.finance.api;

import com.smatech.finance.domain.Budget;
import com.smatech.finance.domain.Transaction;
import com.smatech.finance.dtos.finance.*;
import com.smatech.finance.enums.Category;
import com.smatech.finance.service.FinanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
//@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Finance Management", description = "APIs for managing financial transactions and budgets")
public class FinanceController {

    private final FinanceService financeService;
    private final ModelMapper modelMapper;

    @PostMapping("/transactions")
    @Operation(
            summary = "Create a new transaction",
            description = "Creates a new financial transaction for the authenticated user",
            parameters = {
                    @Parameter(
                            name = "X-User-Id",
                            description = "User identifier",
                            required = true,
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string", example = "admin@finance.com")
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token")
    })
    public ResponseEntity<TransactionDTO> createTransaction(
            @RequestBody CreateTransactionRequest request,
            @RequestHeader("X-User-Id") String userId) {

        Transaction transaction = convertToTransaction(request);
        Transaction savedTransaction = financeService.createTransaction(transaction, userId);
        return ResponseEntity.ok(modelMapper.map(savedTransaction, TransactionDTO.class));
    }

    @GetMapping("/transactions")
    @Operation(
            summary = "Get user transactions with filters",
            description = "Retrieves transactions for the user with optional filtering"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token")
    })
    public ResponseEntity<List<TransactionDTO>> getTransactions(
            @RequestHeader("X-User-Id") String userId,
            @Parameter(name = "category", example = "GROCERIES")
            @RequestParam(value = "category", required = false) Category category,
            @Parameter(name = "startDate", example = "2025-01-01")
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(name = "endDate", example = "2025-12-31")
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(name = "minAmount", example = "10.00")
            @RequestParam(value = "minAmount", required = false) BigDecimal minAmount,
            @Parameter(name = "maxAmount", example = "1000.00")
            @RequestParam(value = "maxAmount", required = false) BigDecimal maxAmount) {

        List<Transaction> transactions = financeService.getTransactions(
                userId, category, startDate, endDate, minAmount, maxAmount
        );
        return ResponseEntity.ok(transactions.stream()
                .map(transaction -> modelMapper.map(transaction, TransactionDTO.class))
                .toList());
    }

    @PostMapping("/budgets")
    @Operation(
            summary = "Create a new budget",
            description = "Creates a new monthly budget for a specific category",
            parameters = {
                    @Parameter(
                            name = "X-User-Id",
                            description = "User identifier",
                            required = true,
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string", example = "admin@finance.com")
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budget created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token")
    })
    public ResponseEntity<BudgetDTO> createBudget(
            @RequestBody CreateBudgetRequest request,
            @RequestHeader("X-User-Id") String userId) {

        Budget budget = convertToBudget(request);
        Budget savedBudget = financeService.createBudget(budget, userId);
        return ResponseEntity.ok(modelMapper.map(savedBudget, BudgetDTO.class));
    }

    @PutMapping("/budgets")
    @Operation(
            summary = "Update a budget",
            description = "Updates an existing budget",
            parameters = {
                    @Parameter(
                            name = "X-User-Id",
                            description = "User identifier",
                            required = true,
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string", example = "admin@finance.com")
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budget updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token")
    })
    public ResponseEntity<BudgetDTO> updateBudget(
            @RequestParam(value = "id") Long id,
            @RequestBody UpdateBudgetRequest updateBudgetRequest,
            @RequestHeader("X-User-Id")String userId){
        Budget updatedBudget = financeService.updateBudget(id, updateBudgetRequest, userId);
        return ResponseEntity.ok(modelMapper.map(updatedBudget, BudgetDTO.class));
    }

    @GetMapping("/budgets")
    @Operation(
            summary = "Get user budgets",
            description = "Retrieves all budgets for the authenticated user",
            parameters = {
                    @Parameter(
                            name = "X-User-Id",
                            description = "User identifier",
                            required = true,
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string", example = "admin@finance.com")
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budgets retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token")
    })
    public ResponseEntity<List<BudgetDTO>> getBudgets(@RequestHeader("X-User-Id") String userId) {
        List<Budget> budgets = financeService.getBudgets(userId);
        return ResponseEntity.ok(budgets.stream()
                .map(budget -> modelMapper.map(budget, BudgetDTO.class))
                .toList());
    }

    @GetMapping("/summary")
    @Operation(
            summary = "Get financial summary",
            description = "Retrieves a comprehensive financial summary for the user",
            parameters = {
                    @Parameter(
                            name = "X-User-Id",
                            description = "User identifier",
                            required = true,
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string", example = "admin@finance.com")
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Summary retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token")
    })
    public ResponseEntity<FinancialSummary> getFinancialSummary(@RequestHeader("X-User-Id") String userId) {
        FinancialSummary summary = financeService.getFinancialSummary(userId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/ai-insights")
    @Operation(
            summary = "Get AI financial insights",
            description = "Generates AI-powered insights based on user's financial data",
            parameters = {
                    @Parameter(
                            name = "X-User-Id",
                            description = "User identifier",
                            required = true,
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string", example = "admin@finance.com")
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "AI insights generated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token")
    })
    public ResponseEntity<String> getAIFinancialInsights(@RequestHeader("X-User-Id") String userId) {
        String insights = financeService.getFinancialInsights(userId);
        return ResponseEntity.ok(insights);
    }

    @GetMapping("/ai-budget-recommendations")
    @Operation(
            summary = "Get AI budget recommendations",
            description = "Generates AI-powered budget recommendations based on spending patterns",
            parameters = {
                    @Parameter(
                            name = "X-User-Id",
                            description = "User identifier",
                            required = true,
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string", example = "admin@finance.com")
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recommendations generated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token")
    })
    public ResponseEntity<String> getAIBudgetRecommendations(
            @RequestHeader("X-User-Id") String userId,
            @Parameter(name = "monthlyIncome")
            @RequestParam(value = "monthlyIncome", required = false) BigDecimal monthlyIncome) {
        String recommendations = financeService.getBudgetRecommendations(userId, monthlyIncome);
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
    private Budget convertToBudget(UpdateBudgetRequest request) {
        Budget budget = new Budget();
        budget.setAmount(request.amount());
        budget.setMonth(request.month());
        budget.setYear(request.year());
        return budget;
    }

}