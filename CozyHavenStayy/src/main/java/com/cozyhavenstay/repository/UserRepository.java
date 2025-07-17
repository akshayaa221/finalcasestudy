package com.cozyhavenstay.repository;

import com.cozyhavenstay.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // Indicates that this interface is a "repository"
public interface UserRepository extends JpaRepository<User, Long> {
    // JpaRepository<Entity, IdType> provides CRUD methods

    // Custom query method: Find a user by username
    User findByUsername(String username);

    // Custom query method: Find a user by email
    User findByEmail(String email);

    // Optional: Check if a user exists by username
    boolean existsByUsername(String username);

    // Optional: Check if a user exists by email
    boolean existsByEmail(String email);
}