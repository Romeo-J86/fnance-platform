package com.smatech.finance.domain;

import com.smatech.finance.dtos.auth.enums.UserRole;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 11:24
 * projectName Finance Platform
 **/

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String firstName;
    private String lastName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private List<UserRole> roles = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private boolean enabled = true;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (roles == null || roles.isEmpty()) {
            roles = List.of(UserRole.USER);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addRole(UserRole role) {
        if (roles == null) {
            roles = new ArrayList<>();
        }
        if (!roles.contains(role)) {
            roles.add(role);
        }
    }

    public void removeRole(UserRole role) {
        if (roles != null) {
            roles.remove(role);
        }
    }

    public boolean hasRole(UserRole role) {
        return roles != null && roles.contains(role);
    }
}