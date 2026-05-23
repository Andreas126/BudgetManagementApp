package org.example.budgetmanagementapp.repository;

import org.example.budgetmanagementapp.domain.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByUsername(String username);
    User save(User user);
    void updateVenitLunar(Long id, java.math.BigDecimal venit);
}

