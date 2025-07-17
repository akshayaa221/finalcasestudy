package com.cozyhavenstay.config;

import com.cozyhavenstay.security.jwt.AuthEntryPointJwt;
import com.cozyhavenstay.security.jwt.AuthTokenFilter;
import com.cozyhavenstay.security.jwt.JwtUtils; // Import JwtUtils
import com.cozyhavenstay.security.services.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

    private final AuthEntryPointJwt unauthorizedHandler;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtils jwtUtils; // Inject JwtUtils here

    // Constructor injection for all dependencies
    public WebSecurityConfig(AuthEntryPointJwt unauthorizedHandler, UserDetailsServiceImpl userDetailsService, JwtUtils jwtUtils) {
        this.unauthorizedHandler = unauthorizedHandler;
        this.userDetailsService = userDetailsService;
        this.jwtUtils = jwtUtils; // Initialize JwtUtils
    }

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        // Pass dependencies via constructor
        return new AuthTokenFilter(jwtUtils, userDetailsService);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        // Use the DaoAuthenticationProvider constructor that takes UserDetailsService
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder); // This setter is still used and fine
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Publicly accessible endpoints
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/users/register").permitAll() // Assuming registration is public
                        .requestMatchers("/api/v1/hotels").permitAll()
                        .requestMatchers("/api/v1/hotels/{id}").permitAll()
                        .requestMatchers("/api/v1/hotels/search").permitAll()
                        .requestMatchers("/api/v1/rooms/hotel/{hotelId}").permitAll()
                        .requestMatchers("/api/v1/rooms/{id}").permitAll() // Allow public access to specific room details for booking form
                        .requestMatchers("/api/v1/reviews/hotel/{hotelId}").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                        .requestMatchers("/api/v1/files/**").permitAll() // Assuming file download/upload might be public or handled by other security

                        // Authenticated endpoints (any logged-in user)
                        .requestMatchers("/api/v1/users/profile").authenticated()
                        .requestMatchers("/api/v1/users/update-profile").authenticated()
                        .requestMatchers("/api/v1/users/change-password").authenticated()
                        .requestMatchers("/api/v1/bookings").authenticated() // For creating bookings
                        .requestMatchers("/api/v1/bookings/{id}").authenticated() // For getting specific booking
                        .requestMatchers("/api/v1/bookings/user/{userId}").authenticated() // For user's own bookings
                        .requestMatchers("/api/v1/reviews").authenticated() // For submitting reviews
                        .requestMatchers("/api/v1/reviews/{id}").authenticated() // For getting/updating specific review

                        // Role-based access
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/hotel-owner/**").hasAnyRole("HOTEL_OWNER", "ADMIN")
                        .requestMatchers("/api/v1/hotels/manage/**").hasAnyRole("HOTEL_OWNER", "ADMIN")
                        .requestMatchers("/api/v1/rooms/manage/**").hasAnyRole("HOTEL_OWNER", "ADMIN")
                        .requestMatchers("/api/v1/bookings/hotel/{hotelId}").hasAnyRole("HOTEL_OWNER", "ADMIN")
                        // Add more specific role-based rules as needed

                        .anyRequest().authenticated() // All other requests require authentication
                );

        http.authenticationProvider(authenticationProvider(passwordEncoder()));

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
