//equipment

package com.compulin.rentflow.controller.module1;

import com.compulin.rentflow.entity.module1.equipment;
import com.compulin.rentflow.security.Module1SessionAccess;
import com.compulin.rentflow.service.module1.equipment_service;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/module1/equipment")
public class equipment_controller {

    private final equipment_service equipmentService;

    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "AVAILABLE",
            "RENTED",
            "DAMAGED",
            "LOST",
            "MAINTENANCE",
            "INACTIVE"
    );

    public equipment_controller(equipment_service equipmentService) {
        this.equipmentService = equipmentService;
    }

    private ResponseEntity<?> checkAccess(HttpSession session) {
        Integer companyId =
                Module1SessionAccess.getCompanyId(session);

        if (companyId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in first."));
        }

        if (!Module1SessionAccess.hasAnyRole(
                session, "COMPANY_ADMIN", "RENTAL_OFFICER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message",
                            "You do not have permission to manage equipment."
                    ));
        }

        return null;
    }

    private Integer getCompanyId(HttpSession session) {
        return Module1SessionAccess.getCompanyId(session);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "Equipment status is required."
            );
        }

        String normalized = status.trim().toUpperCase();

        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException(
                    "Invalid equipment status. Allowed values: "
                            + "AVAILABLE, RENTED, DAMAGED, LOST, "
                            + "MAINTENANCE, INACTIVE."
            );
        }

        return normalized;
    }

    private ResponseEntity<?> handleError(Exception e) {
        String message = e.getMessage();

        if (message == null || message.isBlank()) {
            message = "Unable to process the equipment request.";
        }

        String normalized = message.toLowerCase();

        if (normalized.contains("not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", message));
        }

        if (e instanceof IllegalArgumentException) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", message));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "message",
                        "Unable to process the equipment request."
                ));
    }

    private Map<String, Object> toResponse(equipment item) {
        Map<String, Object> response = new HashMap<>();

        response.put("equipmentId", item.getEquipment_id());
        response.put("itemName", item.getItem_name());
        response.put("itemCode", item.getItem_code());
        response.put("equDescription", item.getEqu_description());
        response.put("rentalRate", item.getRental_rate());
        response.put("ratePeriod", item.getRate_period());

        response.put(
                "securityDepositPerUnit",
                item.getSecurity_deposit_per_unit()
        );

        response.put("totalQuantity", item.getTotal_quantity());
        response.put("availableQuantity", item.getAvailable_quantity());
        response.put("equStatus", item.getEqu_status());
        response.put("createdAt", item.getCreated_at());

        if (item.getCategory() != null) {
            response.put(
                    "categoryId",
                    item.getCategory().getCategory_id()
            );

            response.put(
                    "categoryName",
                    item.getCategory().getCategoryName()
            );
        }

        return response;
    }

    private equipment fromRequest(Map<String, Object> request) {
        equipment item = new equipment();

        item.setItem_name(asString(request.get("itemName")));
        item.setItem_code(asString(request.get("itemCode")));
        item.setEqu_description(
                asString(request.get("equDescription"))
        );

        item.setRental_rate(
                toBigDecimal(request.get("rentalRate"))
        );

        item.setRate_period(
                asString(request.get("ratePeriod"))
        );

        item.setSecurity_deposit_per_unit(
                toBigDecimal(request.get("securityDepositPerUnit"))
        );

        item.setTotal_quantity(
                toInteger(request.get("totalQuantity"))
        );

        item.setAvailable_quantity(
                toInteger(request.get("availableQuantity"))
        );

        String status = asString(request.get("equStatus"));

        if (status != null && !status.isBlank()) {
            item.setEqu_status(normalizeStatus(status));
        }

        return item;
    }

    private String asString(Object value) {
        return value == null ? null : value.toString().trim();
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null || value.toString().isBlank()) {
            return null;
        }

        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "A numeric amount is invalid."
            );
        }
    }

    private Integer toInteger(Object value) {
        if (value == null || value.toString().isBlank()) {
            return null;
        }

        try {
            if (value instanceof Number number) {
                return number.intValue();
            }

            return Integer.valueOf(value.toString());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "A quantity is invalid."
            );
        }
    }

    private String validateQuantities(equipment item) {
        Integer total = item.getTotal_quantity();
        Integer available = item.getAvailable_quantity();

        if (total == null || total < 0) {
            return "Total quantity must be zero or greater.";
        }

        if (available == null
                || available < 0
                || available > total) {
            return "Available quantity must be between zero "
                    + "and total quantity.";
        }

        return null;
    }

    @GetMapping
    public ResponseEntity<?> getAllEquipment(HttpSession session) {
        ResponseEntity<?> accessError = checkAccess(session);

        if (accessError != null) {
            return accessError;
        }

        try {
            List<Map<String, Object>> response = new ArrayList<>();

            List<equipment> items =
                    equipmentService.getEquipmentByCompany(
                            getCompanyId(session)
                    );

            for (equipment item : items) {
                response.add(toResponse(item));
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return handleError(e);
        }
    }

    @GetMapping("/available-for-rental")
    public ResponseEntity<?> getAvailableForRental(
            HttpSession session) {

        ResponseEntity<?> accessError = checkAccess(session);

        if (accessError != null) {
            return accessError;
        }

        try {
            List<Map<String, Object>> response = new ArrayList<>();

            List<equipment> items =
                    equipmentService.getAvailableForRental(
                            getCompanyId(session)
                    );

            for (equipment item : items) {
                response.add(toResponse(item));
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return handleError(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEquipmentById(
            @PathVariable Integer id,
            HttpSession session) {

        ResponseEntity<?> accessError = checkAccess(session);

        if (accessError != null) {
            return accessError;
        }

        try {
            Optional<equipment> result =
                    equipmentService.getEquipmentById(
                            id,
                            getCompanyId(session)
                    );

            if (result.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(toResponse(result.get()));

        } catch (Exception e) {
            return handleError(e);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchEquipment(
            @RequestParam String name,
            HttpSession session) {

        ResponseEntity<?> accessError = checkAccess(session);

        if (accessError != null) {
            return accessError;
        }

        try {
            List<Map<String, Object>> response = new ArrayList<>();

            List<equipment> items =
                    equipmentService.searchEquipment(
                            getCompanyId(session),
                            name.trim()
                    );

            for (equipment item : items) {
                response.add(toResponse(item));
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return handleError(e);
        }
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getEquipmentByCategory(
            @PathVariable Integer categoryId,
            HttpSession session) {

        ResponseEntity<?> accessError = checkAccess(session);

        if (accessError != null) {
            return accessError;
        }

        try {
            List<Map<String, Object>> response = new ArrayList<>();

            List<equipment> items =
                    equipmentService.getEquipmentByCategory(
                            getCompanyId(session),
                            categoryId
                    );

            for (equipment item : items) {
                response.add(toResponse(item));
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return handleError(e);
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getEquipmentByStatus(
            @PathVariable String status,
            HttpSession session) {

        ResponseEntity<?> accessError = checkAccess(session);

        if (accessError != null) {
            return accessError;
        }

        try {
            String normalizedStatus = normalizeStatus(status);

            List<Map<String, Object>> response = new ArrayList<>();

            List<equipment> items =
                    equipmentService.getEquipmentByStatus(
                            getCompanyId(session),
                            normalizedStatus
                    );

            for (equipment item : items) {
                response.add(toResponse(item));
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return handleError(e);
        }
    }

    @PostMapping
    public ResponseEntity<?> createEquipment(
            @RequestBody Map<String, Object> request,
            HttpSession session) {

        ResponseEntity<?> accessError = checkAccess(session);

        if (accessError != null) {
            return accessError;
        }

        try {
            Integer categoryId =
                    toInteger(request.get("categoryId"));

            if (categoryId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "message",
                                "Category is required."
                        ));
            }

            equipment item = fromRequest(request);

            if (item.getEqu_status() == null) {
                item.setEqu_status("AVAILABLE");
            }

            String quantityError = validateQuantities(item);

            if (quantityError != null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", quantityError));
            }

            if (item.getRental_rate() == null
                    || item.getRental_rate().signum() < 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "message",
                                "Rental rate must be zero or greater."
                        ));
            }

            if (item.getSecurity_deposit_per_unit() == null
                    || item.getSecurity_deposit_per_unit().signum() < 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "message",
                                "Security deposit must be zero or greater."
                        ));
            }

            equipment saved =
                    equipmentService.createEquipment(
                            getCompanyId(session),
                            categoryId,
                            item
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(toResponse(saved));

        } catch (Exception e) {
            return handleError(e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEquipment(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> request,
            HttpSession session) {

        ResponseEntity<?> accessError = checkAccess(session);

        if (accessError != null) {
            return accessError;
        }

        try {
            Integer companyId = getCompanyId(session);

            Optional<equipment> existingResult =
                    equipmentService.getEquipmentById(id, companyId);

            if (existingResult.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Integer categoryId =
                    toInteger(request.get("categoryId"));

            if (categoryId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "message",
                                "Category is required."
                        ));
            }

            equipment details = fromRequest(request);

            if (details.getEqu_status() == null) {
                details.setEqu_status(
                        existingResult.get().getEqu_status()
                );
            }

            String quantityError = validateQuantities(details);

            if (quantityError != null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", quantityError));
            }

            if (details.getRental_rate() != null
                    && details.getRental_rate().signum() < 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "message",
                                "Rental rate must be zero or greater."
                        ));
            }

            if (details.getSecurity_deposit_per_unit() != null
                    && details.getSecurity_deposit_per_unit().signum() < 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "message",
                                "Security deposit must be zero or greater."
                        ));
            }

            equipment updated =
                    equipmentService.updateEquipment(
                            id,
                            companyId,
                            categoryId,
                            details
                    );

            return ResponseEntity.ok(toResponse(updated));

        } catch (Exception e) {
            return handleError(e);
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

        try {
            String normalizedStatus = normalizeStatus(status);

            equipment updated =
                    equipmentService.updateStatus(
                            id,
                            getCompanyId(session),
                            normalizedStatus
                    );

            return ResponseEntity.ok(toResponse(updated));

        } catch (Exception e) {
            return handleError(e);
        }
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateEquipment(
            @PathVariable Integer id,
            HttpSession session) {

        ResponseEntity<?> accessError = checkAccess(session);

        if (accessError != null) {
            return accessError;
        }

        try {
            equipment updated =
                    equipmentService.deactivateEquipment(
                            id,
                            getCompanyId(session)
                    );

            return ResponseEntity.ok(toResponse(updated));

        } catch (Exception e) {
            return handleError(e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEquipment(
            @PathVariable Integer id,
            HttpSession session) {

        // Preserve the equipment record for historical references.
        return deactivateEquipment(id, session);
    }
}