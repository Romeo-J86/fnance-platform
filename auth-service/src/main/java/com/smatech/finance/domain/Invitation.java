package com.smatech.finance.domain;

import com.smatech.finance.dtos.auth.enums.UserRole;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * createdBy romeo
 * createdDate 30/11/2025
 * createdTime 09:50
 * projectName Finance Platform
 **/

@Entity
@Table(name = "invitations")
@Data
public class Invitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private String invitedBy; // Email of the inviter

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime usedAt;

    private boolean used = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        token = UUID.randomUUID().toString();
        expiresAt = LocalDateTime.now().plusDays(7);
    }

    public boolean isValid() {
        return !used && LocalDateTime.now().isBefore(expiresAt);
    }
}
