package com.smatech.finance.service;

import com.smatech.finance.domain.User;
import com.smatech.finance.dtos.auth.*;
import com.smatech.finance.dtos.auth.enums.UserRole;

import java.util.List;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 11:54
 * projectName Finance Platform
 **/

public interface AuthService  {
    AuthResponse register(RegisterRequest request);
    AuthResponse registerAdmin(RegisterRequest request);
    AuthResponse registerWithInvitation(RegisterWithInvitationRequest request);
    AuthResponse login(LoginRequest request);
    User getCurrentUser();
    InvitationValidationResponse validateInvitation(String invitationToken);
    void resendOrCreateInvitation(String email, UserRole role, String invitedBy);
   void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
    List<UserRole> getCurrentUserRoles();
    boolean validateCurrentUserPassword(String password);
    void changeCurrentUserPassword(ChangePasswordRequest changePasswordRequest);
    AuthResponse refreshToken(String currentToken);
    boolean hasRole(UserRole role);
}
