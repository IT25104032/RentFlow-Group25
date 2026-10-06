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

        if (!passwordEncoder.matches(password, user.getPassword_hash())) {
            throw new RuntimeException("Invalid email or password");
        }

        return user;
    }
}
