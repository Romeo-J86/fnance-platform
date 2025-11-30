package com.smatech.finance.service;

/**
 * createdBy romeo
 * createdDate 30/11/2025
 * createdTime 08:43
 * projectName Finance Platform
 **/

public interface EmailService {
    void sendWelcomeEmail(String toEmail, String firstName, String temporaryPassword);
    void sendPasswordResetEmail(String toEmail, String firstName, String resetToken);
    void sendInvitationEmail(String toEmail, String invitedBy, String invitationToken);
    void sendAdminWelcomeEmail(String toEmail, String firstName, String temporaryPassword);
}
