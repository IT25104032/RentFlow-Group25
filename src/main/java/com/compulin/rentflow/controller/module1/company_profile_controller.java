
package com.compulin.rentflow.controller.module1;

import com.compulin.rentflow.entity.module1.company;
import com.compulin.rentflow.repository.module1.CompanyRepo;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/module1/company-profile")
public class company_profile_controller {

    private final CompanyRepo companyRepo;

    public company_profile_controller(CompanyRepo companyRepo) {
        this.companyRepo = companyRepo;
    }

    @GetMapping
    public ResponseEntity<?> getProfile(HttpSession session) {
        Integer companyId = getCompanyId(session);
        String role = getRole(session);

        if (companyId == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Please log in first."));
        }

        if (!"COMPANY_ADMIN".equals(role)) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Access denied."));
        }

        Optional<company> result = companyRepo.findById(companyId);

        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toResponse(result.get()));
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(
            @RequestBody Map<String, String> body,
            HttpSession session) {

        Integer companyId = getCompanyId(session);
        String role = getRole(session);

        if (companyId == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Please log in first."));
        }

        if (!"COMPANY_ADMIN".equals(role)) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Access denied."));
        }

        Optional<company> result = companyRepo.findById(companyId);

        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String name = body.get("companyName");
        String phone = body.get("phone");
        String address = body.get("address");

        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Company name is required."
                    ));
        }

        company existing = result.get();

        existing.setCompany_name(name.trim());
        existing.setPhone(phone == null ? "" : phone.trim());
        existing.setAddress(address == null ? "" : address.trim());

        company saved = companyRepo.save(existing);

        return ResponseEntity.ok(toResponse(saved));
    }

    private Integer getCompanyId(HttpSession session) {
        Object value = session.getAttribute("RENTFLOW_COMPANY_ID");

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }

    private String getRole(HttpSession session) {
        Object value = session.getAttribute("RENTFLOW_ROLE");

        if (!(value instanceof String)) {
            return "";
        }

        String role = ((String) value)
                .trim()
                .toUpperCase(Locale.ROOT);

        if (role.startsWith("ROLE_")) {
            role = role.substring(5);
        }

        return role;
    }

    private Map<String, Object> toResponse(company c) {
        Map<String, Object> data = new LinkedHashMap<>();

        data.put("companyId", c.getCompany_id());
        data.put("companyName", c.getCompany_name());
        data.put("registrationNo", c.getRegistration_no());
        data.put("email", c.getEmail());
        data.put("phone", c.getPhone());
        data.put("address", c.getAddress());
        data.put("registrationDate", c.getRegistration_date());
        data.put("companyStatus", c.getCompany_status());

        return data;
    }
}
