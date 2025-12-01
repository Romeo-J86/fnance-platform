package com.smatech.finance.dtos.finance;

import com.smatech.finance.enums.AlertSeverity;
import com.smatech.finance.enums.Category;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 15:01
 * projectName Finance Platform
 **/

@Builder
public record BudgetAlert(
        String message,
        Category category,
        BigDecimal budgetedAmount,
        BigDecimal actualSpent,
        BigDecimal percentageUsed,
        AlertSeverity alertSeverity,
        String recommendation,
        LocalDateTime alertTime
) {}
