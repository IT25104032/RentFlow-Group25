package com.compulin.rentflow.service;

import com.compulin.rentflow.entity.module1.sys_user;
import com.compulin.rentflow.repository.module1.sys_user_repo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final sys_user_repo userRepo;
    private final PasswordEncoder passwordEncoder;

    public AuthService(sys_user_repo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public sys_user login(String email, String password) {

        sys_user user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!"ACTIVE".equalsIgnoreCase(user.getUser_status())) {
            throw new RuntimeException("User account is not active");
        }

        if (user.getCompany() != null &&
                !"ACTIVE".equalsIgnoreCase(user.getCompany().getCompany_status())) {
            throw new RuntimeException("Company account is not active");
        }

        if (!passwordEncoder.matches(password, user.getPassword_hash())) {
            throw new RuntimeException("Invalid email or password");
        }

        return user;
    }

    public void changePassword(Integer userId, String currentPassword, String newPassword) {
        if (userId == null) {
            throw new IllegalArgumentException("Your login session is invalid. Please log in again.");
        }

        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long.");
        }

        if (newPassword.length() > 72) {
            throw new IllegalArgumentException("New password must not exceed 72 characters.");
        }

        sys_user user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User account was not found."));

        if (!"ACTIVE".equalsIgnoreCase(user.getUser_status())) {
            throw new IllegalArgumentException("Your user account is not active.");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword_hash())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword_hash())) {
            throw new IllegalArgumentException("Your new password must be different from your current password.");
        }

        user.setPassword_hash(passwordEncoder.encode(newPassword));
        userRepo.save(user);
    }
}
