package org.example.budgetmanagementapp.service;

import org.example.budgetmanagementapp.domain.User;
import org.example.budgetmanagementapp.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> authenticate(String username, String password) {
        String hashedPassword = hashPassword(password);
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(hashedPassword)) {
            return userOpt;
        }
        return Optional.empty();
    }

    public User register(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username-ul exista deja");
        }
        String hashedPassword = hashPassword(password);
        User user = new User(username, hashedPassword, null);
        return userRepository.save(user);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Eroare la hashing-ul parolei", e);
        }
    }
}
