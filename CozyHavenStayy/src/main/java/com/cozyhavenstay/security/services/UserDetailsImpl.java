package com.cozyhavenstay.security.services;

import com.cozyhavenstay.model.User; // Import your User model
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;


// This class adapts our application's User entity to Spring Security's UserDetails interface.
public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String email; // Using email as the Spring Security username

    @JsonIgnore // Prevent password from being serialized to JSON responses
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String email, String password,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email; // Spring Security's 'username' will be our email
        this.password = password;
        this.authorities = authorities;
    }

    // Factory method to build UserDetailsImpl from our User entity
    public static UserDetailsImpl build(User user) {
        // Convert our User's Role enum to Spring Security's GrantedAuthority
        // Spring Security convention is to prefix roles with "ROLE_"
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        return new UserDetailsImpl(
                user.getId(),
                user.getEmail(),
                user.getPassword(), // This is the hashed password from the DB
                authorities);
    }

    // --- Getters required by UserDetails interface ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public Long getId() { // Custom getter to expose user ID
        return id;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email; // Spring Security uses this for the principal name, which is our user's email
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // For simplicity, assume accounts don't expire
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // For simplicity, assume accounts are not locked
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // For simplicity, assume credentials don't expire
    }

    @Override
    public boolean isEnabled() {
        return true; // For simplicity, assume accounts are always enabled
    }

    // Override equals and hashCode for proper comparison in collections
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}