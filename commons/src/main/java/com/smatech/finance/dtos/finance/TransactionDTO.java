package com.smatech.finance.dtos.finance;

import com.smatech.finance.enums.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 15:15
 * projectName Finance Platform
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private Long id;
    private String userId;
    private BigDecimal amount;
    private String merchant;
    private String description;
    private Category category;
    private LocalDateTime transactionDate;
    private LocalDateTime createdAt;
}