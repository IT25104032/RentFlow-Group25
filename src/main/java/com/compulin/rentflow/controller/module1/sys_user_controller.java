package com.compulin.rentflow.controller.module1;

import com.compulin.rentflow.entity.module1.company;
import com.compulin.rentflow.entity.module1.sys_user;
import com.compulin.rentflow.repository.module1.CompanyRepo;
import com.compulin.rentflow.repository.module1.sys_user_repo;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/module1/users")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
public class sys_user_controller {

    private final sys_user_repo userRepo;
    private final CompanyRepo companyRepo;
    private final PasswordEncoder passwordEncoder;

    public sys_user_controller(
            sys_user_repo userRepo,
            CompanyRepo companyRepo,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepo = userRepo;
        this.companyRepo = companyRepo;
        this.passwordEncoder = passwordEncoder;
    }

    private boolean isCompanyAdmin(HttpSession session) {

        String role =
                (String) session.getAttribute("RENTFLOW_ROLE");

        return "COMPANY_ADMIN".equalsIgnoreCase(role);
    }

    private Integer getCompanyId(HttpSession session) {

        return (Integer) session.getAttribute(
                "RENTFLOW_COMPANY_ID"
        );
    }

    @GetMapping
    public ResponseEntity<?> getCompanyUsers(
            HttpSession session
    ) {

        if (!isCompanyAdmin(session)) {
            return ResponseEntity.status(403)
                    .body(Map.of(
                            "message",
                            "Only Company Admin can manage users"
                    ));
        }

        Integer companyId = getCompanyId(session);

        if (companyId == null) {
            return ResponseEntity.status(403)
                    .body(Map.of(
                            "message",
                            "Company information not found"
                    ));
        }

        return ResponseEntity.ok(
                userRepo.findByCompanyId(companyId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(
            @PathVariable Integer id,
            HttpSession session
    ) {

        if (!isCompanyAdmin(session)) {
            return ResponseEntity.status(403)
                    .body(Map.of(
                            "message",
                            "Access denied"
                    ));
        }

        Integer companyId = getCompanyId(session);

        if (companyId == null) {
            return ResponseEntity.status(403)
                    .body(Map.of(
                            "message",
                            "Company information not found"
                    ));
        }

        Optional<sys_user> user =
                userRepo.findByUserIdAndCompanyId(
                        id,
                        companyId
                );

        return user.<ResponseEntity<?>>map(
                        ResponseEntity::ok
                )
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    @PostMapping
    public ResponseEntity<?> createUser(
            @RequestBody sys_user newUser,
            HttpSession session
    ) {

        if (!isCompanyAdmin(session)) {
            return ResponseEntity.status(403)
                    .body(Map.of(
                            "message",
                            "Only Company Admin can create users"
                    ));
        }

        Integer companyId = getCompanyId(session);

        if (companyId == null) {
            return ResponseEntity.status(403)
                    .body(Map.of(
                            "message",
                            "Company information not found"
                    ));
        }

        if (newUser.getFull_name() == null ||
                newUser.getFull_name().isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Full name is required"
                    ));
        }

        if (newUser.getEmail() == null ||
                newUser.getEmail().isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Email is required"
                    ));
        }

        if (newUser.getPassword_hash() == null ||
                newUser.getPassword_hash().isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Password is required"
                    ));
        }

        String role = newUser.getUser_role();

        if (!"COMPANY_ADMIN".equalsIgnoreCase(role) &&
                !"RENTAL_OFFICER".equalsIgnoreCase(role)) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Invalid company user role"
                    ));
        }

        if (userRepo.findByEmail(
                newUser.getEmail()
        ).isPresent()) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Email already exists"
                    ));
        }

        company userCompany =
                companyRepo.findById(companyId)
                        .orElse(null);

        if (userCompany == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Company not found"
                    ));
        }

        newUser.setCompany(userCompany);

        newUser.setPassword_hash(
                passwordEncoder.encode(
                        newUser.getPassword_hash()
                )
        );

        newUser.setUser_status("ACTIVE");
        newUser.setCreated_at(
                LocalDateTime.now()
        );

        sys_user savedUser =
                userRepo.save(newUser);

        return ResponseEntity.ok(savedUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Integer id,
            @RequestBody sys_user userDetails,
            HttpSession session
    ) {

        if (!isCompanyAdmin(session)) {
            return ResponseEntity.status(403)
                    .body(Map.of(
                            "message",
                            "Only Company Admin can update users"
                    ));
        }

        Integer companyId = getCompanyId(session);

        if (companyId == null) {
            return ResponseEntity.status(403)
                    .body(Map.of(
                            "message",
                            "Company information not found"
                    ));
        }

        sys_user existing =
                userRepo.findByUserIdAndCompanyId(
                        id,
                        companyId
                ).orElse(null);

        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        existing.setFull_name(
                userDetails.getFull_name()
        );

        existing.setPhone(
                userDetails.getPhone()
        );

        String role = userDetails.getUser_role();

        if (role != null &&
                !role.isBlank()) {

            if (!"COMPANY_ADMIN".equalsIgnoreCase(role) &&
                    !"RENTAL_OFFICER".equalsIgnoreCase(role)) {

                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "message",
                                "Invalid company user role"
                        ));
            }

            existing.setUser_role(role);
        }

        if (userDetails.getUser_status() != null &&
                !userDetails.getUser_status().isBlank()) {

            existing.setUser_status(
                    userDetails.getUser_status()
            );
        }

        String password =
                userDetails.getPassword_hash();

        if (password != null &&
                !password.isBlank()) {

            existing.setPassword_hash(
                    passwordEncoder.encode(password)
            );
        }

        sys_user savedUser =
                userRepo.save(existing);

        return ResponseEntity.ok(savedUser);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body,
            HttpSession session
    ) {

        if (!isCompanyAdmin(session)) {
            return ResponseEntity.status(403)
                    .body(Map.of(
                            "message",
                            "Only Company Admin can change user status"
                    ));
        }

        Integer companyId = getCompanyId(session);

        if (companyId == null) {
            return ResponseEntity.status(403)
                    .body(Map.of(
                            "message",
                            "Company information not found"
                    ));
        }

        sys_user existing =
                userRepo.findByUserIdAndCompanyId(
                        id,
                        companyId
                ).orElse(null);

        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        String status = body.get("status");

        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Status is required"
                    ));
        }

        existing.setUser_status(status);

        return ResponseEntity.ok(
                userRepo.save(existing)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deactivateUser(
            @PathVariable Integer id,
            HttpSession session
    ) {

        if (!isCompanyAdmin(session)) {
            return ResponseEntity.status(403)
                    .body(Map.of(
                            "message",
                            "Only Company Admin can deactivate users"
                    ));
        }

        Integer companyId = getCompanyId(session);

        if (companyId == null) {
            return ResponseEntity.status(403)
                    .body(Map.of(
                            "message",
                            "Company information not found"
                    ));
        }

        sys_user existing =
                userRepo.findByUserIdAndCompanyId(
                        id,
                        companyId
                ).orElse(null);

        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        existing.setUser_status("INACTIVE");

        userRepo.save(existing);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "User deactivated successfully"
                )
        );
    }
}