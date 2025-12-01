package com.smatech.finance.config;

import com.smatech.finance.dtos.auth.TokenValidationResponse;
import com.smatech.finance.dtos.auth.UserResponse;
import com.smatech.finance.feign.AuthServiceClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 15:50
 * projectName Finance Platform
 **/

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthServiceClient authServiceClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = parseJwt(request);
            if (jwt != null) {
                TokenValidationResponse validationResponse =
                        authServiceClient.validateToken("Bearer " + jwt);

                if (validationResponse.valid()) {
                    String email = validationResponse.email();

                    UserResponse userResponse =
                            authServiceClient.getCurrentUser("Bearer " + jwt);

                    if (userResponse != null) {
                        List<SimpleGrantedAuthority> authorities = userResponse.roles().stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                .collect(Collectors.toList());

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(email, null, authorities);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.debug("Authenticated user: {} with roles: {} for endpoint: {}",
                                email, userResponse.roles(), request.getRequestURI());
                    }
                } else {
                    log.warn("Invalid JWT token: {}", validationResponse.message());
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/api-docs") ||
                path.startsWith("/webjars") ||
                path.startsWith("/actuator/health") ||
                path.startsWith("/actuator/info") ||
                path.startsWith("/h2-console");
    }
}