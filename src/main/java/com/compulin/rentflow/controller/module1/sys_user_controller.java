package com.compulin.rentflow.controller.module1;

import com.compulin.rentflow.entity.module1.sys_user;
import com.compulin.rentflow.repository.module1.sys_user_repo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/module1/users")
public class sys_user_controller {

    private final sys_user_repo userRepo;
    private final PasswordEncoder passwordEncoder;

    public sys_user_controller(
            sys_user_repo userRepo,
            PasswordEncoder passwordEncoder) {

        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<sys_user> getAllUsers() {
        return userRepo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<sys_user> getUserById(
            @PathVariable Integer id) {

        Optional<sys_user> user = userRepo.findById(id);

        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/company/{companyId}")
    public List<sys_user> getUsersByCompany(
            @PathVariable Integer companyId) {

        return userRepo.findByCompanyCompanyId(companyId);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<sys_user> getUserByEmail(
            @PathVariable String email) {

        Optional<sys_user> user = userRepo.findByEmail(email);

        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createUser(
            @RequestBody sys_user newUser) {

        if (userRepo.findByEmail(newUser.getEmail()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body("Email already exists");
        }

        if ("COMPULIN_ADMIN".equals(newUser.getUser_role())) {
            return ResponseEntity.badRequest()
                    .body("COMPULIN_ADMIN cannot be created from the company user page");
        }

        if (newUser.getPassword_hash() == null ||
                newUser.getPassword_hash().isBlank()) {

            return ResponseEntity.badRequest()
                    .body("Password is required");
        }

        newUser.setPassword_hash(
                passwordEncoder.encode(newUser.getPassword_hash())
        );

        if (newUser.getUser_status() == null ||
                newUser.getUser_status().isBlank()) {

            newUser.setUser_status("ACTIVE");
        }

        newUser.setCreated_at(LocalDateTime.now());

        return ResponseEntity.ok(userRepo.save(newUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Integer id,
            @RequestBody sys_user userDetails) {

        Optional<sys_user> result = userRepo.findById(id);

        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        sys_user existing = result.get();

        existing.setFull_name(userDetails.getFull_name());
        existing.setEmail(userDetails.getEmail());
        existing.setPhone(userDetails.getPhone());
        existing.setUser_role(userDetails.getUser_role());
        existing.setUser_status(userDetails.getUser_status());

        if (userDetails.getPassword_hash() != null &&
                !userDetails.getPassword_hash().isBlank()) {

            existing.setPassword_hash(
                    passwordEncoder.encode(
                            userDetails.getPassword_hash()
                    )
            );
        }

        return ResponseEntity.ok(userRepo.save(existing));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Integer id,
            @RequestParam String status) {

        Optional<sys_user> result = userRepo.findById(id);

        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        sys_user user = result.get();
        user.setUser_status(status);

        return ResponseEntity.ok(userRepo.save(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Integer id) {

        if (!userRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        userRepo.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}