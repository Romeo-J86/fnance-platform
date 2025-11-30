package com.smatech.finance.persistence;

import com.smatech.finance.domain.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * createdBy romeo
 * createdDate 30/11/2025
 * createdTime 09:51
 * projectName Finance Platform
 **/

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    Optional<Invitation> findByToken(String token);

    Optional<Invitation> findByEmailAndUsedFalse(String email);

    List<Invitation> findByInvitedBy(String invitedBy);

    List<Invitation> findByUsedFalseAndExpiresAtBefore(LocalDateTime date);

    @Query("SELECT i FROM Invitation i WHERE i.email = :email AND i.used = false AND i.expiresAt > :now")
    Optional<Invitation> findValidInvitationByEmail(@Param("email") String email, @Param("now") LocalDateTime now);

    boolean existsByEmailAndUsedFalse(String email);

    @Query("SELECT COUNT(i) FROM Invitation i WHERE i.used = false AND i.expiresAt > :now")
    long countActiveInvitations(@Param("now") LocalDateTime now);

    @Query("SELECT i FROM Invitation i WHERE i.email = :email AND i.used = false AND i.expiresAt < :date")
    List<Invitation> findByEmailAndUsedFalseAndExpiresAtBefore(@Param("email") String email, @Param("date") LocalDateTime date);

    List<Invitation> findByEmail(String email);

    @Query("SELECT i FROM Invitation i WHERE i.email = :email AND i.used = false ORDER BY i.createdAt DESC")
    List<Invitation> findActiveInvitationsByEmail(@Param("email") String email);
}
