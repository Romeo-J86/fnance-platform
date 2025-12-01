package com.smatech.finance.service.impl;

import com.smatech.finance.domain.Invitation;
import com.smatech.finance.domain.User;
import com.smatech.finance.dtos.auth.*;
import com.smatech.finance.dtos.auth.enums.UserRole;
import com.smatech.finance.jwt.JwtUtil;
import com.smatech.finance.service.AuthService;
import com.smatech.finance.service.EmailService;
import com.smatech.finance.service.UserService;
import com.smatech.finance.util.PasswordGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 12:13
 * projectName Finance Platform
 **/

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final PasswordGeneratorService passwordGeneratorService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.email());

        try {
            String temporaryPassword = passwordGeneratorService.generateStrongPassword();
            log.info("Generated temporary password for user: {}", request.email());

            // Create user with auto-generated password
            User user = userService.registerUser(
                    request.email(),
                    temporaryPassword,
                    request.firstName(),
                    request.lastName()
            );

            emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName(), temporaryPassword);

            // Generate JWT token
            String token = generateTokenWithUserDetails(user);

            log.info("User registered successfully: {}", request.email());
            return new AuthResponse(
                    token,
                    "Registration successful. Check your email for temporary password.",
                    user.getEmail(),
                    user.getRoles(),
                    user.getFirstName(),
                    user.getLastName(),
                    true // indicates temporary password
            );

        } catch (Exception e) {
            log.error("Registration failed for email: {}", request.email(), e);
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public AuthResponse registerAdmin(RegisterRequest request) {
        log.info("Registering new admin with email: {}", request.email());

        try {
            String temporaryPassword = passwordGeneratorService.generateStrongPassword(14); // Longer for admin

            log.info("Generated temporary admin password for: {}", request.email());

            User user = userService.registerAdmin(
                    request.email(),
                    passwordGeneratorService.generateMemorablePassword(),
                    request.firstName(),
                    request.lastName()
            );

            emailService.sendAdminWelcomeEmail(user.getEmail(), user.getFirstName(), temporaryPassword);
            String token = generateTokenWithUserDetails(user);

            log.info("Admin registered successfully: {}", request.email());
            return new AuthResponse(
                    token,
                    "Admin registration successful",
                    user.getEmail(),
                    user.getRoles(),
                    user.getFirstName(),
                    user.getLastName(),
                    true
            );

        } catch (Exception e) {
            log.error("Admin registration failed for email: {}", request.email(), e);
            throw new RuntimeException("Admin registration failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public AuthResponse registerWithInvitation(RegisterWithInvitationRequest request) {
        log.info("Registration with invitation for: {}", request.email());

        // For invited users, they set their own password
        User user = userService.registerWithInvitation(
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName(),
                request.invitationToken()
        );

        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName(),
                "You set your own password during registration.");

        String token = generateTokenWithUserDetails(user);

        log.info("User registered with invitation successfully: {}", request.email());
        return new AuthResponse(
                token,
                "Registration with invitation successful",
                user.getEmail(),
                user.getRoles(),
                user.getFirstName(),
                user.getLastName(),
                false // user set their own password
        );
    }


    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.email());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userService.findByEmail(request.email())
                    .orElseThrow(() -> new RuntimeException("User not found after authentication: %s"
                            .formatted(request.email())));

            String token = generateTokenWithUserDetails(user);

            log.info("User logged in successfully: {}", request.email());
            return new AuthResponse(
                    token,
                    "Login successful",
                    user.getEmail(),
                    user.getRoles(),
                    user.getFirstName(),
                    user.getLastName(),
                    false
            );

        } catch (Exception e) {
            log.error("Login failed for user: {}", request.email(), e);
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("No authenticated user found");
        }

        String email = authentication.getName();
        return userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public InvitationValidationResponse validateInvitation(String invitationToken) {
        log.info("Validating invitation token: {}", invitationToken);

        try {
            Invitation invitation = userService.validateInvitation(invitationToken, null);

            return InvitationValidationResponse.builder()
                    .valid(true)
                    .message("Invitation is valid")
                    .email(invitation.getEmail())
                    .role(invitation.getRole())
                    .invitedBy(invitation.getInvitedBy())
                    .expiresAt(invitation.getExpiresAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();

        } catch (Exception e) {
            return InvitationValidationResponse.builder()
                    .valid(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public void resendOrCreateInvitation(String email, UserRole role, String invitedBy) {
        log.info("Resending or creating new invitation for: {} with role: {} by: {}", email, role, invitedBy);

        try {
            if (userService.findByEmail(email).isPresent()) {
                throw new RuntimeException("User already exists with email: " + email);
            }

            var existingInvitation = userService.findValidInvitationByEmail(email);

            if (existingInvitation.isPresent()) {
                Invitation invitation = existingInvitation.get();
                emailService.sendInvitationEmail(email, invitation.getInvitedBy(), invitation.getToken());
                log.info("Existing invitation resent for: {}", email);
            } else {
                userService.cleanupExpiredInvitationsForEmail(email);

                Invitation newInvitation = userService.createInvitation(email, role, invitedBy);
                emailService.sendInvitationEmail(email, invitedBy, newInvitation.getToken());
                log.info("New invitation created and sent for: {}", email);
            }

        } catch (Exception e) {
            log.error("Failed to resend/create invitation for: {}", email, e);
            throw new RuntimeException("Failed to process invitation: " + e.getMessage());
        }
    }

    @Override
    public boolean hasRole(UserRole role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_" + role.name()));
    }

    @Override
    public List<UserRole> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return List.of();
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.replace("ROLE_", ""))
                .map(UserRole::valueOf)
                .collect(Collectors.toList());
    }

    @Override
    public boolean validateCurrentUserPassword(String password) {
        try {
            User currentUser = getCurrentUser();
            return userService.validatePassword(currentUser.getEmail(), password);
        } catch (Exception e) {
            log.error("Error validating current user password", e);
            return false;
        }
    }

    @Override
    @Transactional
    public void changeCurrentUserPassword(String newPassword) {
        try {
            User currentUser = getCurrentUser();
            userService.changePassword(currentUser.getEmail(), newPassword);
            log.info("Password changed successfully for user: {}", currentUser.getEmail());
        } catch (Exception e) {
            log.error("Error changing current user password", e);
            throw new RuntimeException("Failed to change password: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String currentToken) {
        try {
            if (!jwtUtil.validateToken(currentToken)) {
                throw new RuntimeException("Invalid token");
            }

            String email = jwtUtil.extractEmail(currentToken);
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found: " + email));

            String newToken = generateTokenWithUserDetails(user);

            log.info("Token refreshed for user: {}", email);
            return new AuthResponse(
                    newToken,
                    "Token refreshed successfully",
                    user.getEmail(),
                    user.getRoles(),
                    user.getFirstName(),
                    user.getLastName(),
                    false
            );

        } catch (Exception e) {
            log.error("Token refresh failed", e);
            throw new RuntimeException("Token refresh failed: " + e.getMessage());
        }
    }

    private String generateTokenWithUserDetails(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRoles());
        claims.put("firstName", user.getFirstName());
        claims.put("lastName", user.getLastName());
        claims.put("userId", user.getId());

        return jwtUtil.generateToken(user.getEmail(), claims);
    }
}