package com.smatech.finance.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 14:51
 * projectName Finance Platform
 **/

@Entity
@Table(name = "transactions")
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    private String merchant;
    private String description;
    private String category;

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
    }
}
