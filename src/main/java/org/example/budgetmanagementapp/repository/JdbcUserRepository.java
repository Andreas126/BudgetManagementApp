package org.example.budgetmanagementapp.repository;

import org.example.budgetmanagementapp.domain.User;
import org.example.budgetmanagementapp.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Optional;

public class JdbcUserRepository implements UserRepository {

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, password, venit_lunar FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setVenitLunar(rs.getBigDecimal("venit_lunar"));
                return Optional.of(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la cautarea utilizatorului", e);
        }
        return Optional.empty();
    }

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users (username, password, venit_lunar) VALUES (?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setBigDecimal(3, user.getVenitLunar() != null ? user.getVenitLunar() : BigDecimal.ZERO);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                user.setId(rs.getLong("id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la salvarea utilizatorului", e);
        }
        return user;
    }
}
