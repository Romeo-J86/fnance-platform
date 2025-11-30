package com.smatech.finance.service.impl;

import com.smatech.finance.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * createdBy romeo
 * createdDate 30/11/2025
 * createdTime 08:45
 * projectName Finance Platform
 **/

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from:noreply@smatechfinance.com}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${spring.mail.enabled:false}")
    private boolean emailEnabled;

    @Override
    @Async
    public void sendWelcomeEmail(String toEmail, String firstName, String temporaryPassword) {
        String subject = "Welcome to SmaTech Finance - Your Account is Ready!";
        String text = buildWelcomeEmailText(firstName, temporaryPassword);

        sendEmail(toEmail, subject, text);
    }

    @Async
    public void sendAdminWelcomeEmail(String toEmail, String firstName, String temporaryPassword) {
        String subject = "Admin Account Created - SmaTech Finance";
        String text = buildAdminWelcomeEmailText(firstName, temporaryPassword);

        sendEmail(toEmail, subject, text);
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String firstName, String resetToken) {
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
        String subject = "Password Reset Request - SmaTech Finance";
        String text = buildPasswordResetEmailText(firstName, resetLink);

        sendEmail(toEmail, subject, text);
    }

    @Async
    public void sendInvitationEmail(String toEmail, String invitedBy, String invitationToken) {
        String invitationLink = frontendUrl + "/accept-invitation?token=" + invitationToken;
        String subject = "You're Invited to Join SmaTech Finance!";
        String text = buildInvitationEmailText(invitedBy, invitationLink);

        sendEmail(toEmail, subject, text);
    }

    private void sendEmail(String toEmail, String subject, String text) {
        if (emailEnabled) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(toEmail);
                message.setSubject(subject);
                message.setText(text);

                mailSender.send(message);
                log.info("Email sent successfully to: {}", toEmail);
            } catch (Exception e) {
                log.error("Failed to send email to: {}", toEmail, e);
            }
        } else {
            logEmailContent(toEmail, subject, text);
        }
    }

    private void logEmailContent(String toEmail, String subject, String text) {
        log.info("=== EMAIL CONTENT (Development Mode) ===");
        log.info("To: {}", toEmail);
        log.info("Subject: {}", subject);
        log.info("Body:\n{}", text);
        log.info("=== END EMAIL ===");
    }

    private String buildWelcomeEmailText(String firstName, String temporaryPassword) {
        return """
            Welcome to SmaTech Finance! 🎉
            
            Hello %s,
            
            Your personal finance management account has been successfully created!
            
            Your Temporary Password: %s
            
            Important Security Steps:
            1. Login using your email and the temporary password above
            2. Change your password immediately after first login
            3. Never share your password with anyone
            
            Login URL: %s/login
            
            If you have any questions, feel free to contact our support team.
            
            Best regards,
            The SmaTech Finance Team
            
            This is an automated message. Please do not reply to this email.
            """.formatted(firstName, temporaryPassword, frontendUrl);
    }

    private String buildAdminWelcomeEmailText(String firstName, String temporaryPassword) {
        return """
            Admin Account Created 🔐
           \s
            Hello %s,
           \s
            Your administrator account for SmaTech Finance has been created successfully.
           \s
            SECURITY NOTICE: As an administrator, you have elevated privileges.\s
            Please ensure you follow security best practices.
           \s
            Your Temporary Password: %s
           \s
            Required Actions:
            1. Login using your email and the temporary password above
            2. Change your password immediately after first login
            3. Enable two-factor authentication for enhanced security
            4. Review and understand your administrative responsibilities
           \s
            Admin Login URL: %s/admin/login
           \s
            Best regards,
            SmaTech Finance Security Team
           \s""".formatted(firstName, temporaryPassword, frontendUrl);
    }

    private String buildPasswordResetEmailText(String firstName, String resetLink) {
        return """
            Password Reset Request
            
            Hello %s,
            
            We received a request to reset your password for your SmaTech Finance account.
            
            Reset your password here: %s
            
            This link will expire in 1 hour for security reasons.
            
            If you didn't request a password reset, please ignore this email or contact support if you have concerns.
            
            Best regards,
            SmaTech Finance Team
            """.formatted(firstName, resetLink);
    }

    private String buildInvitationEmailText(String invitedBy, String invitationLink) {
        return """
            You're Invited! 🎉
            
            Join SmaTech Finance
            
            You have been invited by %s to join SmaTech Finance Platform!
            
            SmaTech Finance helps you manage your personal finances, track spending, and achieve your financial goals with AI-powered insights.
            
            Accept your invitation here: %s
            
            This invitation will expire in 7 days.
            
            Welcome aboard!
            The SmaTech Finance Team
            """.formatted(invitedBy, invitationLink);
    }
}
