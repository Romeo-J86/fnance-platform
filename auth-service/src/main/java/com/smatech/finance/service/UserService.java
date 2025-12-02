package com.smatech.finance.service;

import com.smatech.finance.domain.Invitation;
import com.smatech.finance.domain.User;
import com.smatech.finance.dtos.auth.enums.UserRole;
import com.smatech.finance.persistence.InvitationRepository;
import com.smatech.finance.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 11:49
 * projectName Finance Platform
 **/

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final InvitationRepository invitationRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: %s"
                        .formatted(email)));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList()))
                .disabled(!user.isEnabled())
                .build();
    }

    public boolean userHasRole(String email, UserRole role) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        return user.getRoles().contains(role);
    }

    @Transactional
    public User registerWithInvitation(String email, String password, String firstName,
                                       String lastName, String invitationToken) {
        log.info("Processing invitation registration for email: {} with token: {}", email, invitationToken);



        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User already exists with email: " + email);
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);

        Invitation invitation = validateInvitation(invitationToken, email);

        user.setRoles(List.of(invitation.getRole()));

        User savedUser = createUser(user);

        markInvitationAsUsed(invitation);

        log.info("User successfully registered with invitation. Email: {}, Role: {}",
                email, invitation.getRole());

        return savedUser;
    }

    @Transactional(readOnly = true)
    public Invitation validateInvitation(String invitationToken, String email) {

        Invitation invitation = invitationRepository.findByToken(invitationToken)
                .orElseThrow(() -> new RuntimeException("Invalid invitation token"));


        if (!invitation.isValid()) {
            throw new RuntimeException("Invitation has expired or has already been used");
        }

        if (!invitation.getEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Invitation email does not match provided email");
        }

        return invitation;
    }

    @Transactional
    public void markInvitationAsUsed(Invitation invitation) {
        invitation.setUsed(true);
        invitation.setUsedAt(LocalDateTime.now());
        invitationRepository.save(invitation);

        log.info("Marked invitation as used. Token: {}, Email: {}",
                invitation.getToken(), invitation.getEmail());
    }


    @Transactional
    public Invitation createInvitation(String email, UserRole role, String invitedBy) {
        log.info("Creating invitation for email: {} with role: {} by: {}", email, role, invitedBy);


        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User already exists with email: " + email);
        }

        if (invitationRepository.existsByEmailAndUsedFalse(email)) {
            throw new RuntimeException("Active invitation already exists for: " + email);
        }

        Invitation invitation = new Invitation();
        invitation.setEmail(email);
        invitation.setRole(role);
        invitation.setInvitedBy(invitedBy);

        Invitation savedInvitation = invitationRepository.save(invitation);

        log.info("Created invitation successfully. Token: {}, Email: {}",
                savedInvitation.getToken(), email);

        return savedInvitation;
    }

    @Transactional(readOnly = true)
    public List<Invitation> getUserInvitations(String invitedBy) {
        return invitationRepository.findByInvitedBy(invitedBy);
    }
    @Transactional
    public void cleanupExpiredInvitationsForEmail(String email) {
        LocalDateTime now = LocalDateTime.now();
        List<Invitation> expired = invitationRepository.findByEmailAndUsedFalseAndExpiresAtBefore(email, now);

        if (!expired.isEmpty()) {
            invitationRepository.deleteAll(expired);
            log.info("Cleaned up {} expired invitations for email: {}", expired.size(), email);
        }
    }

    @Transactional(readOnly = true)
    public List<Invitation> findByEmailAndUsedFalseAndExpiresAtBefore(String email, LocalDateTime date) {
        return invitationRepository.findByEmailAndUsedFalseAndExpiresAtBefore(email, date);
    }

    @Transactional
    public void cancelInvitation(String token) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        invitationRepository.delete(invitation);
        log.info("Cancelled invitation for: {}", invitation.getEmail());
    }

    @Transactional
    public void cancelAllInvitationsForEmail(String email) {
        List<Invitation> invitations = invitationRepository.findByEmail(email);
        invitationRepository.deleteAll(invitations);
        log.info("Cancelled all invitations for: {}", email);
    }

    @Transactional(readOnly = true)
    public Optional<Invitation> findValidInvitationByEmail(String email) {
        return invitationRepository.findValidInvitationByEmail(email, LocalDateTime.now());
    }

    @Transactional
    public void cleanupExpiredInvitations() {
        LocalDateTime now = LocalDateTime.now();
        List<Invitation> expired = invitationRepository.findByUsedFalseAndExpiresAtBefore(now);

        if (!expired.isEmpty()) {
            invitationRepository.deleteAll(expired);
            log.info("Cleaned up {} expired invitations", expired.size());
        }
    }

    @Transactional
    public User createUserWithInvitation(User user, String invitationToken) {
        Invitation invitation = validateInvitation(invitationToken, user.getEmail());

        user.setRoles(List.of(invitation.getRole()));

        User savedUser = createUser(user);

        markInvitationAsUsed(invitation);

        return savedUser;
    }


    @Transactional
    public User createUser(User user) {
        validateUser(user);

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists with email: " + user.getEmail());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        log.info("Created new user with email: {} and roles: {}", savedUser.getEmail(), savedUser.getRoles());
        return savedUser;
    }

    @Transactional
    public User registerUser(String email, String password, String firstName, String lastName, UserRole userRole) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRoles(List.of(userRole));

        return createUser(user);
    }

    @Transactional
    public User registerAdmin(String email, String password, String firstName, String lastName) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRoles(List.of(UserRole.ADMIN, UserRole.USER));

        return createUser(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<User> findByRole(String role) {
        return userRepository.findByRolesContaining(role);
    }

    @Transactional
    public User updateUserRoles(String email, List<UserRole> roles) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        user.setRoles(roles);
        User updatedUser = userRepository.save(user);

        log.info("Updated roles for user: {} to: {}", email, roles);
        return updatedUser;
    }

    @Transactional
    public User addRoleToUser(String email, UserRole role) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        user.addRole(role);
        User updatedUser = userRepository.save(user);

        log.info("Added role {} to user: {}", role, email);
        return updatedUser;
    }

    @Transactional
    public User removeRoleFromUser(String email, UserRole role) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        user.removeRole(role);
        User updatedUser = userRepository.save(user);

        log.info("Removed role {} from user: {}", role, email);
        return updatedUser;
    }

    @Transactional
    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        userRepository.delete(user);
        log.info("Deleted user: {}", email);
    }

    @Transactional
    public User updateUserProfile(String email, String firstName, String lastName) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        user.setFirstName(firstName);
        user.setLastName(lastName);

        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed for user: {}", email);
    }

    public boolean validatePassword(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        if (!isValidEmail(user.getEmail())) {
            throw new RuntimeException("Invalid email format");
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}
