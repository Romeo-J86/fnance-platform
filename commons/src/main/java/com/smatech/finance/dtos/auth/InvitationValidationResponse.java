package com.smatech.finance.dtos.auth;

import com.smatech.finance.dtos.auth.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * createdBy romeo
 * createdDate 30/11/2025
 * createdTime 11:46
 * projectName Finance Platform
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed response for invitation validation")
public class InvitationValidationResponse {

    @Schema(description = "Whether the invitation token is valid", example = "true")
    private boolean valid;

    @Schema(description = "Validation status code", example = "VALID")
    private String status;

    @Schema(description = "Human-readable validation message", example = "Invitation is valid and ready to use")
    private String message;

    @Schema(description = "Email address associated with the invitation", example = "user@example.com")
    private String email;

    @Schema(description = "Role assigned by the invitation", example = "USER")
    private UserRole role;

    @Schema(description = "Email of the user who sent the invitation", example = "admin@company.com")
    private String invitedBy;

    @Schema(description = "Expiration date of the invitation", example = "2024-12-31T23:59:59")
    private String expiresAt;

    @Schema(description = "Invitation creation date", example = "2024-01-15T10:30:00")
    private String createdAt;

    @Schema(description = "Time when the invitation was used (if applicable)", example = "2024-01-16T14:30:00")
    private String usedAt;

    @Schema(description = "Days remaining until expiration", example = "5")
    private Long daysRemaining;

    @Schema(description = "Whether the invitation has been used", example = "false")
    private boolean used;

    @Schema(description = "Invitation token (for reference)", example = "abc123-def456-ghi789")
    private String token;

//    // Static factory methods
//    public static InvitationValidationResponse valid(com.smatech.finance..Invitation invitation) {
//        long daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), invitation.getExpiresAt());
//
//        return InvitationValidationResponse.builder()
//                .valid(true)
//                .status("VALID")
//                .message("Invitation is valid and ready to use")
//                .email(invitation.getEmail())
//                .role(invitation.getRole())
//                .invitedBy(invitation.getInvitedBy())
//                .expiresAt(invitation.getExpiresAt().toString())
//                .createdAt(invitation.getCreatedAt().toString())
//                .usedAt(invitation.getUsedAt() != null ? invitation.getUsedAt().toString() : null)
//                .daysRemaining(daysRemaining)
//                .used(invitation.isUsed())
//                .token(invitation.getToken())
//                .build();
//    }

    public static InvitationValidationResponse invalid(String message) {
        return InvitationValidationResponse.builder()
                .valid(false)
                .status("INVALID")
                .message(message)
                .build();
    }

    public static InvitationValidationResponse expired() {
        return InvitationValidationResponse.builder()
                .valid(false)
                .status("EXPIRED")
                .message("Invitation has expired")
                .build();
    }

    public static InvitationValidationResponse alreadyUsed() {
        return InvitationValidationResponse.builder()
                .valid(false)
                .status("USED")
                .message("Invitation has already been used")
                .build();
    }

    public static InvitationValidationResponse notFound() {
        return InvitationValidationResponse.builder()
                .valid(false)
                .status("NOT_FOUND")
                .message("Invitation token not found")
                .build();
    }
}
