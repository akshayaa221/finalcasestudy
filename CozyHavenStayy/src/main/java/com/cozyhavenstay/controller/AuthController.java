package com.cozyhavenstay.controller;

import com.cozyhavenstay.security.jwt.JwtUtils;
import com.cozyhavenstay.security.payload.request.LoginRequest;
import com.cozyhavenstay.security.payload.response.JwtResponse;
import com.cozyhavenstay.security.services.UserDetailsImpl; // Our custom UserDetails implementation
import jakarta.validation.Valid; // For @Valid annotation

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth") // Base path for authentication endpoints
@CrossOrigin(origins = "http://localhost:4200") // Allow requests from your Angular frontend
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    // No @Autowired needed for single constructor
    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Handles user login, authenticates credentials, and returns a JWT token.
     * @param loginRequest DTO containing user's email and password.
     * @return ResponseEntity with JwtResponse containing token and user details.
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // Authenticate the user using Spring Security's AuthenticationManager
        // This will trigger UserDetailsServiceImpl.loadUserByUsername() and compare passwords
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        // If authentication is successful, set the authentication in the security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT token
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Get authenticated user details (our custom UserDetailsImpl)
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Extract roles from the authenticated user
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Return JWT response with token and user info
        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(), // This is the email
                roles));
    }
}