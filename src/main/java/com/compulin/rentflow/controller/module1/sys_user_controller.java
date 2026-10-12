//sys_user

package com.compulin.rentflow.controller.module1;

import com.compulin.rentflow.entity.module1.company;
import com.compulin.rentflow.entity.module1.sys_user;
import com.compulin.rentflow.repository.module1.CompanyRepo;
import com.compulin.rentflow.repository.module1.sys_user_repo;
import com.compulin.rentflow.security.Module1SessionAccess;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

    private static final Set<String> ALLOWED_ROLES =
            Set.of("COMPANY_ADMIN", "RENTAL_OFFICER");

    private static final Set<String> ALLOWED_STATUSES =
            Set.of("ACTIVE", "INACTIVE");

    public sys_user_controller(
            sys_user_repo userRepo,
            CompanyRepo companyRepo,
            PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.companyRepo = companyRepo;
        this.passwordEncoder = passwordEncoder;
    }

    private ResponseEntity<?> checkLogin(HttpSession session) {
        String role = Module1SessionAccess.getRole(session);

        if (role.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in first."));
        }

        if (!Set.of(
                "COMPULIN_ADMIN",
                "COMPANY_ADMIN",
                "RENTAL_OFFICER"
        ).contains(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Access denied."));
        }

        if (!role.equals("COMPULIN_ADMIN")
                && Module1SessionAccess.getCompanyId(session) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Company session is missing."));
        }

        return null;
    }

    private ResponseEntity<?> checkCompanyAdmin(HttpSession session) {
        ResponseEntity<?> error = checkLogin(session);

        if (error != null) {
            return error;
        }

        if (!Module1SessionAccess.hasAnyRole(
                session, "COMPANY_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message",
                            "Only a Company Administrator can manage users."
                    ));
        }

        return null;
    }

    private boolean canAccessUser(
            sys_user user, HttpSession session) {

        if (Module1SessionAccess.hasAnyRole(
                session, "COMPULIN_ADMIN")) {
            return true;
        }

        Integer companyId =
                Module1SessionAccess.getCompanyId(session);

        return user.getCompany() != null
                && companyId != null
                && companyId.equals(
                user.getCompany().getCompany_id());
    }

    private Optional<sys_user> findAccessibleUser(
            Integer userId, HttpSession session) {

        if (Module1SessionAccess.hasAnyRole(
                session, "COMPULIN_ADMIN")) {
            return userRepo.findById(userId);
        }

        Integer companyId =
                Module1SessionAccess.getCompanyId(session);

        if (companyId == null) {
            return Optional.empty();
        }

        return userRepo.findByUserIdAndCompanyId(
                userId, companyId);
    }

    private Map<String, Object> toResponse(sys_user user) {
        Map<String, Object> data = new HashMap<>();

        data.put("user_id", user.getUser_id());
        data.put("userId", user.getUser_id());

        data.put("full_name", user.getFull_name());
        data.put("fullName", user.getFull_name());

        data.put("email", user.getEmail());
        data.put("phone", user.getPhone());

        data.put("user_role", user.getUser_role());
        data.put("userRole", user.getUser_role());

        data.put("user_status", user.getUser_status());
        data.put("userStatus", user.getUser_status());

        data.put("created_at", user.getCreated_at());
        data.put("createdAt", user.getCreated_at());

        Integer companyId = user.getCompany() == null
                ? null
                : user.getCompany().getCompany_id();

        data.put("company_id", companyId);
        data.put("companyId", companyId);

        return data;
    }

    private List<Map<String, Object>> toResponses(
            List<sys_user> users) {

        List<Map<String, Object>> response = new ArrayList<>();

        for (sys_user user : users) {
            response.add(toResponse(user));
        }

        return response;
    }

    private String normalizeRole(String role) {
        if (role == null) {
            throw new IllegalArgumentException("User role is required.");
        }

        String value = role.trim().toUpperCase(Locale.ROOT);

        if (!ALLOWED_ROLES.contains(value)) {
            throw new IllegalArgumentException(
                    "Role must be COMPANY_ADMIN or RENTAL_OFFICER."
            );
        }

        return value;
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            throw new IllegalArgumentException("User status is required.");
        }

        String value = status.trim().toUpperCase(Locale.ROOT);

        if (!ALLOWED_STATUSES.contains(value)) {
            throw new IllegalArgumentException(
                    "Status must be ACTIVE or INACTIVE."
            );
        }

        return value;
    }

    private ResponseEntity<?> badRequest(String message) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", message));
    }

    @GetMapping
    public ResponseEntity<?> getCompanyUsers(HttpSession session) {
        ResponseEntity<?> error = checkLogin(session);

        if (error != null) {
            return error;
        }

        if (Module1SessionAccess.hasAnyRole(
                session, "COMPULIN_ADMIN")) {
            return ResponseEntity.ok(
                    toResponses(userRepo.findAll()));
        }

        Integer companyId =
                Module1SessionAccess.getCompanyId(session);

        return ResponseEntity.ok(
                toResponses(
                        userRepo.findByCompanyCompanyId(companyId)));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<?> getUsersByCompany(
            @PathVariable Integer companyId,
            HttpSession session) {

        ResponseEntity<?> error = checkLogin(session);

        if (error != null) {
            return error;
        }

        if (!Module1SessionAccess.hasAnyRole(
                session, "COMPULIN_ADMIN")) {

            Integer sessionCompanyId =
                    Module1SessionAccess.getCompanyId(session);

            if (sessionCompanyId == null
                    || !sessionCompanyId.equals(companyId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "message",
                                "You cannot access another company's users."
                        ));
            }
        }

        return ResponseEntity.ok(
                toResponses(
                        userRepo.findByCompanyCompanyId(companyId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(
            @PathVariable Integer id,
            HttpSession session) {

        ResponseEntity<?> error = checkLogin(session);

        if (error != null) {
            return error;
        }

        Optional<sys_user> user =
                findAccessibleUser(id, session);

        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toResponse(user.get()));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(
            @PathVariable String email,
            HttpSession session) {

        ResponseEntity<?> error = checkLogin(session);

        if (error != null) {
            return error;
        }

        Optional<sys_user> user = userRepo.findByEmail(email);

        if (user.isEmpty() || !canAccessUser(user.get(), session)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toResponse(user.get()));
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<?> getUsersByRole(
            @PathVariable String role,
            HttpSession session) {

        ResponseEntity<?> error = checkLogin(session);

        if (error != null) {
            return error;
        }

        String normalized = role.trim().toUpperCase(Locale.ROOT);

        if (!ALLOWED_ROLES.contains(normalized)
                && !normalized.equals("COMPULIN_ADMIN")) {
            return badRequest("Invalid user role.");
        }

        List<sys_user> users =
                userRepo.findByUserRole(normalized);

        users.removeIf(user -> !canAccessUser(user, session));

        return ResponseEntity.ok(toResponses(users));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getUsersByStatus(
            @PathVariable String status,
            HttpSession session) {

        ResponseEntity<?> error = checkLogin(session);

        if (error != null) {
            return error;
        }

        try {
            String normalized = normalizeStatus(status);

            List<sys_user> users =
                    userRepo.findByUserStatus(normalized);

            users.removeIf(user -> !canAccessUser(user, session));

            return ResponseEntity.ok(toResponses(users));

        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createUser(
            @RequestBody sys_user newUser,
            HttpSession session) {

        ResponseEntity<?> error = checkCompanyAdmin(session);

        if (error != null) {
            return error;
        }

        try {
            if (newUser.getFull_name() == null
                    || newUser.getFull_name().isBlank()) {
                return badRequest("Full name is required.");
            }

            if (newUser.getEmail() == null
                    || newUser.getEmail().isBlank()
                    || !newUser.getEmail().contains("@")) {
                return badRequest("A valid email address is required.");
            }

            String password = newUser.getPassword_hash();

            if (password == null || password.isBlank()) {
                return badRequest("Password is required.");
            }

            if (password.length() < 8) {
                return badRequest(
                        "Password must be at least 8 characters long.");
            }

            newUser.setUser_role(
                    normalizeRole(newUser.getUser_role()));

            if (userRepo.findByEmail(newUser.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "Email already exists."));
            }

            Integer companyId =
                    Module1SessionAccess.getCompanyId(session);

            Optional<company> companyRecord =
                    companyRepo.findById(companyId);

            if (companyRecord.isEmpty()) {
                return badRequest("Company not found.");
            }

            newUser.setCompany(companyRecord.get());
            newUser.setPassword_hash(
                    passwordEncoder.encode(password));
            newUser.setUser_status("ACTIVE");
            newUser.setCreated_at(LocalDateTime.now());

            sys_user saved = userRepo.save(newUser);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(toResponse(saved));

        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Integer id,
            @RequestBody sys_user details,
            HttpSession session) {

        ResponseEntity<?> error = checkCompanyAdmin(session);

        if (error != null) {
            return error;
        }

        Optional<sys_user> result =
                findAccessibleUser(id, session);

        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            sys_user existing = result.get();

            if (details.getFull_name() != null
                    && !details.getFull_name().isBlank()) {
                existing.setFull_name(details.getFull_name().trim());
            }

            if (details.getEmail() != null
                    && !details.getEmail().isBlank()) {

                String email = details.getEmail().trim();

                if (!email.contains("@")) {
                    return badRequest("Invalid email address.");
                }

                Optional<sys_user> emailOwner =
                        userRepo.findByEmail(email);

                if (emailOwner.isPresent()
                        && !emailOwner.get().getUser_id()
                        .equals(existing.getUser_id())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(Map.of("message", "Email already exists."));
                }

                existing.setEmail(email);
            }

            if (details.getPhone() != null) {
                existing.setPhone(details.getPhone());
            }

            if (details.getUser_role() != null
                    && !details.getUser_role().isBlank()) {
                existing.setUser_role(
                        normalizeRole(details.getUser_role()));
            }

            if (details.getUser_status() != null
                    && !details.getUser_status().isBlank()) {
                existing.setUser_status(
                        normalizeStatus(details.getUser_status()));
            }

            String password = details.getPassword_hash();

            if (password != null && !password.isBlank()) {
                if (password.length() < 8) {
                    return badRequest(
                            "Password must be at least 8 characters long.");
                }

                existing.setPassword_hash(
                        passwordEncoder.encode(password));
            }

            sys_user saved = userRepo.save(existing);

            return ResponseEntity.ok(toResponse(saved));

        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body,
            HttpSession session) {

        ResponseEntity<?> error = checkCompanyAdmin(session);

        if (error != null) {
            return error;
        }

        Optional<sys_user> result =
                findAccessibleUser(id, session);

        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            String status = normalizeStatus(body.get("status"));

            sys_user user = result.get();
            user.setUser_status(status);

            return ResponseEntity.ok(
                    toResponse(userRepo.save(user)));

        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deactivateUser(
            @PathVariable Integer id,
            HttpSession session) {

        ResponseEntity<?> error = checkCompanyAdmin(session);

        if (error != null) {
            return error;
        }

        Optional<sys_user> result =
                findAccessibleUser(id, session);

        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        sys_user user = result.get();
        user.setUser_status("INACTIVE");
        userRepo.save(user);

        return ResponseEntity.ok(
                Map.of("message", "User deactivated successfully."));
    }
}
