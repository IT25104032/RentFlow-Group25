package com.compulin.rentflow.controller.module1;

import com.compulin.rentflow.entity.module1.equipment;
import com.compulin.rentflow.service.module1.equipment_service;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/module1/equipment")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
public class equipment_controller {

    private final equipment_service equipmentService;

    public equipment_controller(
            equipment_service equipmentService
    ) {
        this.equipmentService = equipmentService;
    }

    private boolean canManageEquipment(
            HttpSession session
    ) {

        String role =
                (String) session.getAttribute(
                        "RENTFLOW_ROLE"
                );

        return "COMPANY_ADMIN".equalsIgnoreCase(role)
                || "RENTAL_OFFICER".equalsIgnoreCase(role);
    }

    private Integer getCompanyId(
            HttpSession session
    ) {

        return (Integer) session.getAttribute(
                "RENTFLOW_COMPANY_ID"
        );
    }

    @GetMapping
    public ResponseEntity<?> getEquipment(
            HttpSession session
    ) {

        if (!canManageEquipment(session)) {
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
                equipmentService.getEquipmentByCompany(
                        companyId
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEquipmentById(
            @PathVariable Integer id,
            HttpSession session
    ) {

        if (!canManageEquipment(session)) {
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
                    equipmentService.getEquipmentById(
                            id,
                            companyId
                    )
            );

        } catch (RuntimeException ex) {

            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchEquipment(
            @RequestParam String name,
            HttpSession session
    ) {

        if (!canManageEquipment(session)) {
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
                equipmentService.searchEquipment(
                        companyId,
                        name
                )
        );
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getEquipmentByCategory(
            @PathVariable Integer categoryId,
            HttpSession session
    ) {

        if (!canManageEquipment(session)) {
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
                equipmentService.getEquipmentByCategory(
                        companyId,
                        categoryId
                )
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getEquipmentByStatus(
            @PathVariable String status,
            HttpSession session
    ) {

        if (!canManageEquipment(session)) {
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
                equipmentService.getEquipmentByStatus(
                        companyId,
                        status
                )
        );
    }

    @PostMapping
    public ResponseEntity<?> createEquipment(
            @RequestBody Map<String, Object> request,
            HttpSession session
    ) {

        if (!canManageEquipment(session)) {
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

            equipment newEquipment =
                    new equipment();

            newEquipment.setItem_name(
                    (String) request.get("item_name")
            );

            newEquipment.setItem_code(
                    (String) request.get("item_code")
            );

            newEquipment.setEqu_description(
                    (String) request.get("equ_description")
            );

            if (request.get("rental_rate") != null) {
                newEquipment.setRental_rate(
                        new java.math.BigDecimal(
                                request
                                        .get("rental_rate")
                                        .toString()
                        )
                );
            }

            newEquipment.setRate_period(
                    (String) request.get("rate_period")
            );

            if (request.get(
                    "refundable_deposit_per_unit"
            ) != null) {

                newEquipment
                        .setRefundable_deposit_per_unit(
                                new java.math.BigDecimal(
                                        request
                                                .get(
                                                        "refundable_deposit_per_unit"
                                                )
                                                .toString()
                                )
                        );
            }

            if (request.get("total_quantity") != null) {

                newEquipment.setTotal_quantity(
                        Integer.parseInt(
                                request
                                        .get("total_quantity")
                                        .toString()
                        )
                );
            }

            if (request.get("equ_status") != null) {

                newEquipment.setEqu_status(
                        (String) request.get("equ_status")
                );
            }

            Integer categoryId =
                    request.get("category_id") == null
                            ? null
                            : Integer.parseInt(
                            request
                                    .get("category_id")
                                    .toString()
                    );

            if (categoryId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "message",
                                "Category is required"
                        ));
            }

            equipment saved =
                    equipmentService.createEquipment(
                            companyId,
                            categoryId,
                            newEquipment
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
    public ResponseEntity<?> updateEquipment(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> request,
            HttpSession session
    ) {

        if (!canManageEquipment(session)) {
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

            equipment details =
                    new equipment();

            details.setItem_name(
                    (String) request.get("item_name")
            );

            details.setItem_code(
                    (String) request.get("item_code")
            );

            details.setEqu_description(
                    (String) request.get("equ_description")
            );

            if (request.get("rental_rate") != null) {

                details.setRental_rate(
                        new java.math.BigDecimal(
                                request
                                        .get("rental_rate")
                                        .toString()
                        )
                );
            }

            details.setRate_period(
                    (String) request.get("rate_period")
            );

            if (request.get(
                    "refundable_deposit_per_unit"
            ) != null) {

                details.setRefundable_deposit_per_unit(
                        new java.math.BigDecimal(
                                request
                                        .get(
                                                "refundable_deposit_per_unit"
                                        )
                                        .toString()
                        )
                );
            }

            if (request.get("total_quantity") != null) {

                details.setTotal_quantity(
                        Integer.parseInt(
                                request
                                        .get("total_quantity")
                                        .toString()
                        )
                );
            }

            if (request.get("available_quantity") != null) {

                details.setAvailable_quantity(
                        Integer.parseInt(
                                request
                                        .get("available_quantity")
                                        .toString()
                        )
                );
            }

            if (request.get("equ_status") != null) {

                details.setEqu_status(
                        (String) request.get(
                                "equ_status"
                        )
                );
            }

            Integer categoryId =
                    request.get("category_id") == null
                            ? null
                            : Integer.parseInt(
                            request
                                    .get("category_id")
                                    .toString()
                    );

            equipment updated =
                    equipmentService.updateEquipment(
                            id,
                            companyId,
                            categoryId,
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

        if (!canManageEquipment(session)) {
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

        try {

            return ResponseEntity.ok(
                    equipmentService.updateStatus(
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
    public ResponseEntity<?> deactivateEquipment(
            @PathVariable Integer id,
            HttpSession session
    ) {

        if (!canManageEquipment(session)) {
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

            equipmentService.deactivateEquipment(
                    id,
                    companyId
            );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Equipment deactivated successfully"
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