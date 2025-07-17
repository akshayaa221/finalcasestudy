package com.cozyhavenstay.service;

import com.cozyhavenstay.model.User;
import com.cozyhavenstay.model.User.Role; // Import Role enum
import java.util.List;
import java.util.Optional;

public interface UserService {
    User registerUser(User user);
    User findByUsername(String username);
    User findByEmail(String email);
    Optional<User> getUserById(Long id);
    List<User> getAllUsers();
    User updateUser(Long id, User userDetails);
    void deleteUser(Long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> getUsersByRole(Role role); // To fetch guests, hotel owners, admins
}