package com.cozyhavenstay.security.services;

import com.cozyhavenstay.model.User; // Import your User model
import com.cozyhavenstay.repository.UserRepository; // Import your UserRepository

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// This class loads user-specific data during authentication.
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    // No @Autowired needed for single constructor in modern Spring
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional // Ensures the user object is fully loaded from the DB
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // We use email for login as per our User model, but Spring Security calls it 'username'
        User user = userRepository.findByEmail(email); // Assuming findByEmail in UserRepository
        if (user == null) {
            throw new UsernameNotFoundException("User Not Found with email: " + email);
        }

        // Convert our User model into Spring Security's UserDetails object using our custom builder
        return UserDetailsImpl.build(user);
    }
}