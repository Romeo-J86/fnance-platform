package com.smatech.finance.api;

import com.smatech.finance.domain.Invitation;
import com.smatech.finance.domain.User;
import com.smatech.finance.dtos.auth.*;
import com.smatech.finance.dtos.auth.enums.UserRole;
import com.smatech.finance.jwt.JwtUtil;
import com.smatech.finance.service.AuthService;
import com.smatech.finance.service.EmailService;
import com.smatech.finance.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@Tag(name = "Authentication", description = "Authentication and user management endpoints")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;

    @GetMapping("/validate-token")
    @Operation(
            summary = "Validate JWT token",
            description = "Validates the provided JWT token and returns validation status with user email if valid",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Token validation result",
                            content = @Content(schema = @Schema(implementation = TokenValidationResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request")
            }
    )
    public ResponseEntity<TokenValidationResponse> validateToken(
            @Parameter(description = "Bearer token in format: 'Bearer {token}'",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader("Authorization") String authHeader) {
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
    @Operation(
            summary = "Get current authenticated user",
            description = "Returns information about the currently authenticated user",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Current user information retrieved successfully",
                            content = @Content(schema = @Schema(implementation = UserResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
            }
    )
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
    @Operation(
            summary = "Register with invitation token",
            description = "Register a new user using a valid invitation token",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Registration successful",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid invitation token or registration data")
            }
    )
    public ResponseEntity<?> registerWithInvitation(
            @Parameter(description = "Registration data with invitation token", required = true)
            @Valid @RequestBody RegisterWithInvitationRequest request) {
        try {
            AuthResponse response = authService.registerWithInvitation(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/invitations/validate/{token}")
    @Operation(
            summary = "Validate invitation token",
            description = "Check if an invitation token is valid and not expired",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Invitation validation result",
                            content = @Content(schema = @Schema(implementation = InvitationValidationResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid token format")
            }
    )
    public ResponseEntity<InvitationValidationResponse> validateInvitation(
            @Parameter(description = "Invitation token", required = true, example = "abc123-def456-ghi789")
            @PathVariable String token) {
        InvitationValidationResponse response = authService.validateInvitation(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/invitations/resend")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Resend invitation email",
            description = "Resend an invitation email to a user. Available to authenticated users.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Invitation resent successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid email or user not found"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
            }
    )
    public ResponseEntity<?> resendInvitation(
            @Parameter(description = "Email and role for invitation resend", required = true)
            @RequestBody ResendInvitationRequest request) {
        try {
            String invitedBy = authService.getCurrentUser().getEmail();
            authService.resendOrCreateInvitation(request.email(), request.role(), invitedBy);
            return ResponseEntity.ok(new MessageResponse("Invitation resent successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/invitations")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create new invitation",
            description = "Create and send a new invitation. Admin role required.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Invitation created and sent successfully",
                            content = @Content(schema = @Schema(implementation = InvitationResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid email or user already exists"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - not an admin")
            }
    )
    public ResponseEntity<?> createInvitation(
            @Parameter(description = "Invitation creation data", required = true)
            @Valid @RequestBody CreateInvitationRequest request) {
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
    @Operation(
            summary = "Get user's invitations",
            description = "Retrieve all invitations sent by the current user. Admin role required.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of invitations retrieved successfully",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Invitation.class)))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - not an admin")
            }
    )
    public ResponseEntity<List<Invitation>> getUserInvitations() {
        String invitedBy = authService.getCurrentUser().getEmail();
        List<Invitation> invitations = userService.getUserInvitations(invitedBy);
        return ResponseEntity.ok(invitations);
    }

    @DeleteMapping("/invitations/{token}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Cancel invitation",
            description = "Cancel an existing invitation. Admin role required.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Invitation cancelled successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid token or invitation not found"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - not an admin")
            }
    )
    public ResponseEntity<?> cancelInvitation(
            @Parameter(description = "Invitation token to cancel", required = true, example = "abc123-def456-ghi789")
            @PathVariable String token) {
        try {
            userService.cancelInvitation(token);
            return ResponseEntity.ok(new MessageResponse("Invitation cancelled successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register new user",
            description = "Register a new user account (open registration)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Registration successful",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid registration data or user already exists")
            }
    )
    public ResponseEntity<?> register(
            @Parameter(description = "User registration data", required = true)
            @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/register-admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Register new admin user",
            description = "Register a new user with admin privileges. Admin role required.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Admin registration successful",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid registration data"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - not an admin")
            }
    )
    public ResponseEntity<?> registerAdmin(
            @Parameter(description = "Admin registration data", required = true)
            @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.registerAdmin(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticate user and return JWT token",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid credentials"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - invalid email or password")
            }
    )
    public ResponseEntity<?> login(
            @Parameter(description = "Login credentials", required = true)
            @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(
                    new LoginRequest(request.email(), request.password())
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Request password reset",
            description = "Sends a password reset token to the user's email",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Reset link sent if email exists"),
                    @ApiResponse(responseCode = "400", description = "Invalid email format")
            }
    )
    public ResponseEntity<MessageResponse> forgotPassword(
            @Parameter(description = "Email for password reset", required = true)
            @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(new MessageResponse("If an account exists with that email, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Reset password",
            description = "Resets the user's password using a valid token",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Password reset successful"),
                    @ApiResponse(responseCode = "400", description = "Invalid or expired token")
            }
    )
    public ResponseEntity<MessageResponse> resetPassword(
            @Parameter(description = "Password reset data with token and new password", required = true)
            @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(new MessageResponse("Password has been reset successfully."));
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get user profile",
            description = "Retrieve the current user's profile information",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                            content = @Content(schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
            }
    )
    public ResponseEntity<User> getProfile() {
        User user = authService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Update user profile",
            description = "Update the current user's profile information",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                            content = @Content(schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid profile data"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
            }
    )
    public ResponseEntity<User> updateProfile(
            @Parameter(description = "Profile update data", required = true)
            @RequestBody UpdateProfileRequest request) {
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
    @Operation(
            summary = "Change password",
            description = "Changes the current user's password",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Password changed successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid password or validation failed"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
            }
    )
    public ResponseEntity<?> changePassword(
            @Parameter(description = "New password information", required = true)
            @RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(authService.getCurrentUser().getEmail(), request.newPassword());
            return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // Admin-only endpoints

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get all users",
            description = "Retrieve list of all users. Admin role required.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of users retrieved successfully",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - not an admin")
            }
    )
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{email}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update user roles",
            description = "Replace all roles for a user. Admin role required.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "User roles updated successfully",
                            content = @Content(schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid roles or user not found"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - not an admin")
            }
    )
    public ResponseEntity<User> updateUserRoles(
            @Parameter(description = "User email", required = true, example = "user@example.com")
            @PathVariable String email,
            @Parameter(description = "New roles to assign", required = true)
            @RequestBody UpdateRolesRequest request) {
        User updatedUser = userService.updateUserRoles(email, request.roles());
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/users/{email}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Add role to user",
            description = "Add a specific role to a user. Admin role required.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Role added successfully",
                            content = @Content(schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid role or user not found"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - not an admin")
            }
    )
    public ResponseEntity<User> addRoleToUser(
            @Parameter(description = "User email", required = true, example = "user@example.com")
            @PathVariable String email,
            @Parameter(description = "Role to add", required = true)
            @RequestBody UpdateRolesRequest request) {
        User updatedUser = userService.addRoleToUser(email, request.roles().get(0));
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/users/{email}/roles/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Remove role from user",
            description = "Remove a specific role from a user. Admin role required.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Role removed successfully",
                            content = @Content(schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid role or user not found"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - not an admin")
            }
    )
    public ResponseEntity<User> removeRoleFromUser(
            @Parameter(description = "User email", required = true, example = "user@example.com")
            @PathVariable String email,
            @Parameter(description = "Role to remove", required = true, schema = @Schema(implementation = UserRole.class))
            @PathVariable UserRole role) {
        User updatedUser = userService.removeRoleFromUser(email, role);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/users/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete user",
            description = "Delete a user account. Admin role required.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "User deleted successfully"),
                    @ApiResponse(responseCode = "400", description = "User not found"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - not an admin")
            }
    )
    public ResponseEntity<?> deleteUser(
            @Parameter(description = "User email to delete", required = true, example = "user@example.com")
            @PathVariable String email) {
        userService.deleteUser(email);
        return ResponseEntity.ok(new MessageResponse("User deleted successfully"));
    }
}