package com.smatech.finance.dtos.finance;

import com.smatech.finance.enums.Category;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 15:09
 * projectName Finance Platform
 **/

@Builder
public record CreateTransactionRequest(
        BigDecimal amount,
        String merchant,
        String description,
        Category category,
        LocalDateTime transactionDate
) {}
