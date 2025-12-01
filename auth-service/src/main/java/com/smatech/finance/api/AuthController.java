package com.smatech.finance.api;

import com.smatech.finance.domain.Invitation;
import com.smatech.finance.domain.User;
import com.smatech.finance.dtos.auth.*;
import com.smatech.finance.dtos.auth.enums.UserRole;
import com.smatech.finance.jwt.JwtUtil;
import com.smatech.finance.service.AuthService;
import com.smatech.finance.service.EmailService;
import com.smatech.finance.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 12:21
 * projectName Finance Platform
 **/

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;

    @Value("${spring.mail.username}")
    private String email;

    @GetMapping("/validate-token")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.extractEmail(token);
                return ResponseEntity.ok(new TokenValidationResponse(true, email, "Token is valid"));
            } else {
                return ResponseEntity.ok(new TokenValidationResponse(false, null, "Invalid token"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(new TokenValidationResponse(false, null, "Token validation failed"));
        }
    }

    @GetMapping("/current-user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUser() {
        try {
            User user = authService.getCurrentUser();
            return ResponseEntity.ok(new UserResponse(
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRoles()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


    @PostMapping("/register/invitation")
    public ResponseEntity<?> registerWithInvitation(
            @Valid @RequestBody RegisterWithInvitationRequest request) {
        try {
            AuthResponse response = authService.registerWithInvitation(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/invitations/validate/{token}")
    public ResponseEntity<InvitationValidationResponse> validateInvitation(
            @PathVariable String token) {
        InvitationValidationResponse response = authService.validateInvitation(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/invitations/resend")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> resendInvitation(@RequestBody ResendInvitationRequest request) {
        try {
            String invitedBy = authService.getCurrentUser().getEmail();
            authService.resendOrCreateInvitation(request.email(),request.role(), invitedBy);
            return ResponseEntity.ok(new MessageResponse("Invitation resent successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/invitations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createInvitation(@Valid @RequestBody CreateInvitationRequest request) {
        try {
            String invitedBy = authService.getCurrentUser().getEmail();
            Invitation invitation = userService.createInvitation(
                    request.email(),
                    request.role(),
                    invitedBy
            );

            // Send invitation email
            emailService.sendInvitationEmail(request.email(), invitedBy, invitation.getToken());

            return ResponseEntity.ok(new InvitationResponse(
                    invitation.getToken(),
                    invitation.getEmail(),
                    invitation.getRole(),
                    invitation.getExpiresAt()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/invitations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Invitation>> getUserInvitations() {
        String invitedBy = authService.getCurrentUser().getEmail();
        List<Invitation> invitations = userService.getUserInvitations(invitedBy);
        return ResponseEntity.ok(invitations);
    }

    @DeleteMapping("/invitations/{token}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cancelInvitation(@PathVariable String token) {
        try {
            userService.cancelInvitation(token);
            return ResponseEntity.ok(new MessageResponse("Invitation cancelled successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/register-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerAdmin(@RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.registerAdmin(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(
                    new LoginRequest(request.email(), request.password())
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> getProfile() {
        User user = authService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> updateProfile(@RequestBody UpdateProfileRequest request) {
        User currentUser = authService.getCurrentUser();
        User updatedUser = userService.updateUserProfile(
                currentUser.getEmail(),
                request.getFirstName(),
                request.getLastName()
        );
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(authService.getCurrentUser().getEmail(), request.getNewPassword());
            return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // Admin-only endpoints
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{email}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUserRoles(@PathVariable String email, @RequestBody UpdateRolesRequest request) {
        User updatedUser = userService.updateUserRoles(email, request.roles());
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/users/{email}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> addRoleToUser(@PathVariable String email, @RequestBody UpdateRolesRequest request) {
        User updatedUser = userService.addRoleToUser(email, request.roles().get(0));
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/users/{email}/roles/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> removeRoleFromUser(@PathVariable String email, @PathVariable UserRole role) {
        User updatedUser = userService.removeRoleFromUser(email, role);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/users/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable String email) {
        userService.deleteUser(email);
        return ResponseEntity.ok(new MessageResponse("User deleted successfully"));
    }
}