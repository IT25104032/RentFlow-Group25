package com.compulin.rentflow.controller.module1;

import com.compulin.rentflow.entity.module1.company;
import com.compulin.rentflow.entity.module1.equipment_category;
import com.compulin.rentflow.repository.module1.CompanyRepo;
import com.compulin.rentflow.service.module1.equipment_category_service;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/module1/equipment-categories")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
public class equipment_category_controller {

    private final equipment_category_service categoryService;
    private final CompanyRepo companyRepo;

    public equipment_category_controller(
            equipment_category_service categoryService,
            CompanyRepo companyRepo
    ) {
        this.categoryService = categoryService;
        this.companyRepo = companyRepo;
    }

    private boolean canManageCategories(HttpSession session) {

        String role =
                (String) session.getAttribute("RENTFLOW_ROLE");

        return "COMPANY_ADMIN".equalsIgnoreCase(role)
                || "RENTAL_OFFICER".equalsIgnoreCase(role);
    }

    private Integer getCompanyId(HttpSession session) {

        return (Integer) session.getAttribute(
                "RENTFLOW_COMPANY_ID"
        );
    }

    @GetMapping
    public ResponseEntity<?> getCategories(
            HttpSession session
    ) {

        if (!canManageCategories(session)) {
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

        return ResponseEntity.ok(
                categoryService.getCategoriesByCompany(
                        companyId
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(
            @PathVariable Integer id,
            HttpSession session
    ) {

        if (!canManageCategories(session)) {
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

        try {

            return ResponseEntity.ok(
                    categoryService.getCategoryById(
                            id,
                            companyId
                    )
            );

        } catch (RuntimeException ex) {

            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchCategories(
            @RequestParam String name,
            HttpSession session
    ) {

        if (!canManageCategories(session)) {
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

        return ResponseEntity.ok(
                categoryService.searchCategories(
                        companyId,
                        name
                )
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getCategoriesByStatus(
            @PathVariable String status,
            HttpSession session
    ) {

        if (!canManageCategories(session)) {
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

        return ResponseEntity.ok(
                categoryService.getCategoriesByStatus(
                        companyId,
                        status
                )
        );
    }

    @PostMapping
    public ResponseEntity<?> createCategory(
            @RequestBody equipment_category newCategory,
            HttpSession session
    ) {

        if (!canManageCategories(session)) {
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

        try {

            company company =
                    companyRepo.findById(companyId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Company not found"
                                    )
                            );

            newCategory.setCompany(company);

            equipment_category saved =
                    categoryService.createCategory(
                            newCategory
                    );

            return ResponseEntity.ok(saved);

        } catch (RuntimeException ex) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            ex.getMessage()
                    ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(
            @PathVariable Integer id,
            @RequestBody equipment_category details,
            HttpSession session
    ) {

        if (!canManageCategories(session)) {
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

        try {

            equipment_category updated =
                    categoryService.updateCategory(
                            id,
                            companyId,
                            details
                    );

            return ResponseEntity.ok(updated);

        } catch (RuntimeException ex) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            ex.getMessage()
                    ));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body,
            HttpSession session
    ) {

        if (!canManageCategories(session)) {
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

        String status = body.get("status");

        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Status is required"
                    ));
        }

        try {

            return ResponseEntity.ok(
                    categoryService.updateStatus(
                            id,
                            companyId,
                            status
                    )
            );

        } catch (RuntimeException ex) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            ex.getMessage()
                    ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(
            @PathVariable Integer id,
            HttpSession session
    ) {

        if (!canManageCategories(session)) {
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

        try {

            categoryService.deleteCategory(
                    id,
                    companyId
            );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Category deactivated successfully"
                    )
            );

        } catch (RuntimeException ex) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            ex.getMessage()
                    ));
        }
    }
}