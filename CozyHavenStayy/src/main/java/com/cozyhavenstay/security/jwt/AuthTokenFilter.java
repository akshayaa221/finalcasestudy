package com.cozyhavenstay.security.jwt;

import com.cozyhavenstay.security.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AuthTokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils; // Changed to final for constructor injection
    private final UserDetailsServiceImpl userDetailsService; // Changed to final for constructor injection

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    // Constructor injection for dependencies
    public AuthTokenFilter(JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = parseJwt(request); // Extract JWT from request header
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) { // Validate the token
                String username = jwtUtils.getUserNameFromJwtToken(jwt); // Get username (email) from token

                UserDetails userDetails = userDetailsService.loadUserByUsername(username); // Load user details
                // Create authentication object: principal (user details), credentials (null for JWT), authorities
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                // Set the authentication object in Spring Security's context
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }

        filterChain.doFilter(request, response); // Continue the filter chain
    }

    /**
     * Extracts JWT from the Authorization header (Bearer token).
     * @param request The HTTP request.
     * @return The JWT string or null if not found.
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7); // Remove "Bearer " prefix
        }
        return null;
    }
}
