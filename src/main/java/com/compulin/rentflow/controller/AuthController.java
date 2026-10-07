package com.compulin.rentflow.controller;

import com.compulin.rentflow.entity.module1.sys_user;
import com.compulin.rentflow.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {

        session.invalidate();

        return ResponseEntity.ok(
                Map.of("message", "Logged out successfully")
        );
    }
}