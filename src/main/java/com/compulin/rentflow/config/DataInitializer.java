package com.compulin.rentflow.config;

import com.compulin.rentflow.entity.module1.sys_user;
import com.compulin.rentflow.repository.module1.sys_user_repo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner createCompulinAdmin(
            sys_user_repo userRepo,
            PasswordEncoder passwordEncoder
    ) {

        return args -> {

            if (userRepo.findByEmail(
                    "admin@compulin.com"
            ).isPresent()) {
                return;
            }

            sys_user admin = new sys_user();

            admin.setFull_name(
                    "Compulin Administrator"
            );

            admin.setEmail(
                    "admin@compulin.com"
            );

            admin.setPassword_hash(
                    passwordEncoder.encode("123456")
            );

            admin.setPhone(
                    ""
            );

            admin.setUser_role(
                    "COMPULIN_ADMIN"
            );

            admin.setUser_status(
                    "ACTIVE"
            );

            admin.setCreated_at(
                    LocalDateTime.now()
            );

            admin.setCompany(
                    null
            );

            userRepo.save(admin);
        };
    }
}