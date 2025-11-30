package com.smatech.finance.persistence;

import com.smatech.finance.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 11:50
 * projectName Finance Platform
 **/

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE :role MEMBER OF u.roles")
    List<User> findByRolesContaining(@Param("role") String role);

    @Query("SELECT u FROM User u WHERE u.enabled = true")
    List<User> findAllEnabledUsers();

    void deleteByEmail(String email);
}
