
package com.compulin.rentflow.controller.module1;

import com.compulin.rentflow.entity.module1.equipment_category;
import com.compulin.rentflow.security.Module1SessionAccess;
import com.compulin.rentflow.service.module1.equipment_category_service;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/module1/equipment-categories")
public class equipment_category_controller {

    private final equipment_category_service categoryService;

    public equipment_category_controller(
            equipment_category_service categoryService) {
        this.categoryService = categoryService;
    }

    private ResponseEntity<?> checkAccess(HttpSession session) {
        Integer companyId =
                Module1SessionAccess.getCompanyId(session);

        if (companyId == null) {
            return ResponseEntity.status(401)
                    .body(Map.of(
                            "message",
                            "Please log in first."
                    ));
        }

        if (!Module1SessionAccess.hasAnyRole(
                session,
                "COMPANY_ADMIN",
                "RENTAL_OFFICER")) {
            return ResponseEntity.status(403)
                    .body(Map.of(
                            "message",
                            "You do not have permission to access categories."
                    ));
        }

        return null;
    }

    private ResponseEntity<Map<String, String>> badRequest(
            Exception exception) {
        String message = exception.getMessage();

        if (message == null || message.isBlank()) {
            message = "Unable to process the category request.";
        }

        return ResponseEntity.badRequest()
                .body(Map.of("message", message));
    }

    @GetMapping
    public ResponseEntity<?> getCategories(
            HttpSession session) {

        ResponseEntity<?> accessError = checkAccess(session);
        if (accessError != null) {
            return accessError;
        }

        Integer companyId =
                Module1SessionAccess.getCompanyId(session);

        try {
            return ResponseEntity.ok(
                    categoryService.getCategoriesByCompany(companyId)
            );
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchCategories(
            @RequestParam String name,
            HttpSession session) {

        ResponseEntity<?> accessError = checkAccess(session);
        if (accessError != null) {
            return accessError;
        }

        Integer companyId =
                Module1SessionAccess.getCompanyId(session);

        try {
            return ResponseEntity.ok(
                    categoryService.searchCategories(
                            companyId,
                            name
                    )
            );
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getCategoriesByStatus(
            @PathVariable String status,
            HttpSession session) {

        ResponseEntity<?> accessError = checkAccess(session);
        if (accessError != null) {
            return accessError;
        }

        Integer companyId =
                Module1SessionAccess.getCompanyId(session);

        try {
            return ResponseEntity.ok(
                    categoryService.getCategoriesByStatus(
                            companyId,
                            status
                    )
            );
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCategory(
            @PathVariable Integer id,
            HttpSession session) {

        ResponseEntity<?> accessError = checkAccess(session);
        if (accessError != null) {
            return accessError;
        }

        Integer companyId =
                Module1SessionAccess.getCompanyId(session);

        try {
            Optional<equipment_category> category =
                    categoryService.getCategoryById(id, companyId);

            if (category.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(category.get());

        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @PostMapping
    public ResponseEntity<?> createCategory(
            @RequestBody Map<String, Object> request,
            HttpSession session) {

        ResponseEntity<?> accessError = checkAccess(session);
        if (accessError != null) {
            return accessError;
        }

        Integer companyId =
                Module1SessionAccess.getCompanyId(session);

        try {
            String categoryName =
                    (String) request.get("categoryName");

            String description =
                    (String) request.get("catDescription");

            if (categoryName == null || categoryName.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "message",
                                "Category name is required."
                        ));
            }

            return ResponseEntity.ok(
                    categoryService.createCategory(
                            companyId,
                            categoryName,
                            description
                    )
            );

        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> request,
            HttpSession session) {

        ResponseEntity<?> accessError = checkAccess(session);
        if (accessError != null) {
            return accessError;
        }

        Integer companyId =
                Module1SessionAccess.getCompanyId(session);

        try {
            String categoryName =
                    (String) request.get("categoryName");

            String description =
                    (String) request.get("catDescription");

            String status =
                    (String) request.get("catStatus");

            return ResponseEntity.ok(
                    categoryService.updateCategory(
                            id,
                            companyId,
                            categoryName,
                            description,
                            status
                    )
            );

        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Integer id,
            @RequestParam String status,
            HttpSession session) {

        ResponseEntity<?> accessError = checkAccess(session);
        if (accessError != null) {
            return accessError;
        }

        Integer companyId =
                Module1SessionAccess.getCompanyId(session);

        try {
            equipment_category updated =
                    categoryService.updateStatus(
                            id,
                            companyId,
                            status
                    );

            return ResponseEntity.ok(updated);

        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateCategory(
            @PathVariable Integer id,
            HttpSession session) {

        ResponseEntity<?> accessError = checkAccess(session);
        if (accessError != null) {
            return accessError;
        }

        Integer companyId =
                Module1SessionAccess.getCompanyId(session);

        try {
            return ResponseEntity.ok(
                    categoryService.deactivateCategory(
                            id,
                            companyId
                    )
            );

        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateCategory(
            @PathVariable Integer id,
            HttpSession session) {

        ResponseEntity<?> accessError = checkAccess(session);
        if (accessError != null) {
            return accessError;
        }

        Integer companyId =
                Module1SessionAccess.getCompanyId(session);

        try {
            return ResponseEntity.ok(
                    categoryService.activateCategory(
                            id,
                            companyId
                    )
            );

        } catch (Exception e) {
            return badRequest(e);
        }
    }
}
