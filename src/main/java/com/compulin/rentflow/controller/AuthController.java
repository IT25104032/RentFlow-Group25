package com.compulin.rentflow.controller;

import com.compulin.rentflow.entity.module1.sys_user;
import com.compulin.rentflow.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
public class AuthController {

    public static final String SESSION_USER_ID = "RENTFLOW_USER_ID";
    public static final String SESSION_COMPANY_ID = "RENTFLOW_COMPANY_ID";
    public static final String SESSION_ROLE = "RENTFLOW_ROLE";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> loginRequest,
            HttpServletRequest request,
            HttpSession session) {

        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        if (email == null || email.isBlank() ||
                password == null || password.isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Email and password are required"
                    ));
        }

        try {
            sys_user user = authService.login(email, password);

            // Prevent session fixation after successful login.
            request.changeSessionId();

            session.setAttribute(
                    SESSION_USER_ID,
                    user.getUser_id()
            );

            session.setAttribute(
                    SESSION_ROLE,
                    user.getUser_role()
            );

            if (user.getCompany() != null) {
                session.setAttribute(
                        SESSION_COMPANY_ID,
                        user.getCompany().getCompany_id()
                );
            }

            Map<String, Object> response =
                    new LinkedHashMap<>();

            response.put("message", "Login successful");
            response.put("userId", user.getUser_id());
            response.put("fullName", user.getFull_name());
            response.put("email", user.getEmail());
            response.put("role", user.getUser_role());
            response.put("status", user.getUser_status());

            if (user.getCompany() != null) {
                response.put(
                        "companyId",
                        user.getCompany().getCompany_id()
                );
            } else {
                response.put("companyId", null);
            }

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            e.getMessage()
                    ));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody Map<String, String> passwordRequest,
            HttpSession session) {

        Object sessionUserId = session.getAttribute(SESSION_USER_ID);
        if (sessionUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in before changing your password."));
        }

        Integer userId;
        try {
            userId = sessionUserId instanceof Number number
                    ? number.intValue()
                    : Integer.valueOf(sessionUserId.toString());
        } catch (NumberFormatException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Your login session is invalid. Please log in again."));
        }

        String currentPassword = passwordRequest.get("currentPassword");
        String newPassword = passwordRequest.get("newPassword");
        String confirmPassword = passwordRequest.get("confirmPassword");

        if (currentPassword == null || currentPassword.isEmpty()
                || newPassword == null || newPassword.isEmpty()
                || confirmPassword == null || confirmPassword.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "All password fields are required."));
        }

        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "New password and confirmation do not match."));
        }

        try {
            authService.changePassword(userId, currentPassword, newPassword);
            return ResponseEntity.ok(
                    Map.of("message", "Password changed successfully."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {

        session.invalidate();

        return ResponseEntity.ok(
                Map.of("message", "Logged out successfully")
        );
    }
}