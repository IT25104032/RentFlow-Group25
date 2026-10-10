package com.compulin.rentflow.controller.module1;

import com.compulin.rentflow.entity.module1.equipment;
import com.compulin.rentflow.service.module1.equipment_service;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/module1/equipment")
public class equipment_controller {

    private final equipment_service equipmentService;

    public equipment_controller(
            equipment_service equipmentService
    ) {
        this.equipmentService = equipmentService;
    }

    private Integer getCompanyId(
            HttpSession session
    ) {

        Object companyId =
                session.getAttribute(
                        "RENTFLOW_COMPANY_ID"
                );

        if (companyId == null) {
            throw new RuntimeException(
                    "No company is associated with the logged-in user"
            );
        }

        return Integer.valueOf(
                companyId.toString()
        );
    }

    private Map<String, Object> toResponse(
            equipment item
    ) {

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "equipmentId",
                item.getEquipment_id()
        );

        response.put(
                "itemName",
                item.getItem_name()
        );

        response.put(
                "itemCode",
                item.getItem_code()
        );

        response.put(
                "equDescription",
                item.getEqu_description()
        );

        response.put(
                "rentalRate",
                item.getRental_rate()
        );

        response.put(
                "ratePeriod",
                item.getRate_period()
        );

        response.put(
                "securityDepositPerUnit",
                item.getSecurity_deposit_per_unit()
        );

        response.put(
                "totalQuantity",
                item.getTotal_quantity()
        );

        response.put(
                "availableQuantity",
                item.getAvailable_quantity()
        );

        response.put(
                "equStatus",
                item.getEqu_status()
        );

        response.put(
                "createdAt",
                item.getCreated_at()
        );

        if (item.getCategory() != null) {

            response.put(
                    "categoryId",
                    item.getCategory()
                            .getCategory_id()
            );

            response.put(
                    "categoryName",
                    item.getCategory()
                            .getCategoryName()
            );
        }

        return response;
    }

    private equipment fromRequest(
            Map<String, Object> request
    ) {

        equipment item =
                new equipment();

        item.setItem_name(
                (String) request.get(
                        "itemName"
                )
        );

        item.setItem_code(
                (String) request.get(
                        "itemCode"
                )
        );

        item.setEqu_description(
                (String) request.get(
                        "equDescription"
                )
        );

        item.setRental_rate(
                toBigDecimal(
                        request.get(
                                "rentalRate"
                        )
                )
        );

        item.setRate_period(
                (String) request.get(
                        "ratePeriod"
                )
        );

        item.setSecurity_deposit_per_unit(
                toBigDecimal(
                        request.get(
                                "securityDepositPerUnit"
                        )
                )
        );

        item.setTotal_quantity(
                toInteger(
                        request.get(
                                "totalQuantity"
                        )
                )
        );

        item.setAvailable_quantity(
                toInteger(
                        request.get(
                                "availableQuantity"
                        )
                )
        );

        item.setEqu_status(
                (String) request.get(
                        "equStatus"
                )
        );

        return item;
    }

    private BigDecimal toBigDecimal(
            Object value
    ) {

        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {

            return BigDecimal.valueOf(
                    number.doubleValue()
            );
        }

        return new BigDecimal(
                value.toString()
        );
    }

    private Integer toInteger(
            Object value
    ) {

        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.intValue();
        }

        return Integer.valueOf(
                value.toString()
        );
    }

    @GetMapping
    public ResponseEntity<?> getAllEquipment(
            HttpSession session
    ) {

        try {

            Integer companyId =
                    getCompanyId(session);

            List<equipment> equipmentList =
                    equipmentService
                            .getEquipmentByCompany(
                                    companyId
                            );

            List<Map<String, Object>> response =
                    new ArrayList<>();

            for (equipment item : equipmentList) {
                response.add(
                        toResponse(item)
                );
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity
                    .status(401)
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEquipmentById(
            @PathVariable Integer id,
            HttpSession session
    ) {

        try {

            Integer companyId =
                    getCompanyId(session);

            return equipmentService
                    .getEquipmentById(
                            id,
                            companyId
                    )
                    .map(item ->
                            ResponseEntity.ok(
                                    toResponse(item)
                            )
                    )
                    .orElseGet(() ->
                            ResponseEntity
                                    .notFound()
                                    .build()
                    );

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchEquipment(
            @RequestParam String name,
            HttpSession session
    ) {

        try {

            Integer companyId =
                    getCompanyId(session);

            List<equipment> equipmentList =
                    equipmentService
                            .searchEquipment(
                                    companyId,
                                    name
                            );

            List<Map<String, Object>> response =
                    new ArrayList<>();

            for (equipment item : equipmentList) {
                response.add(
                        toResponse(item)
                );
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getEquipmentByCategory(
            @PathVariable Integer categoryId,
            HttpSession session
    ) {

        try {

            Integer companyId =
                    getCompanyId(session);

            List<equipment> equipmentList =
                    equipmentService
                            .getEquipmentByCategory(
                                    companyId,
                                    categoryId
                            );

            List<Map<String, Object>> response =
                    new ArrayList<>();

            for (equipment item : equipmentList) {
                response.add(
                        toResponse(item)
                );
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getEquipmentByStatus(
            @PathVariable String status,
            HttpSession session
    ) {

        try {

            Integer companyId =
                    getCompanyId(session);

            List<equipment> equipmentList =
                    equipmentService
                            .getEquipmentByStatus(
                                    companyId,
                                    status
                            );

            List<Map<String, Object>> response =
                    new ArrayList<>();

            for (equipment item : equipmentList) {
                response.add(
                        toResponse(item)
                );
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    @PostMapping
    public ResponseEntity<?> createEquipment(
            @RequestBody Map<String, Object> request,
            HttpSession session
    ) {

        try {

            Integer companyId =
                    getCompanyId(session);

            Integer categoryId =
                    toInteger(
                            request.get(
                                    "categoryId"
                            )
                    );

            if (categoryId == null) {

                throw new RuntimeException(
                        "Category is required"
                );
            }

            equipment item =
                    fromRequest(request);

            equipment saved =
                    equipmentService.createEquipment(
                            companyId,
                            categoryId,
                            item
                    );

            return ResponseEntity.ok(
                    toResponse(saved)
            );

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEquipment(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> request,
            HttpSession session
    ) {

        try {

            Integer companyId =
                    getCompanyId(session);

            Integer categoryId =
                    toInteger(
                            request.get(
                                    "categoryId"
                            )
                    );

            if (categoryId == null) {

                throw new RuntimeException(
                        "Category is required"
                );
            }

            equipment item =
                    fromRequest(request);

            equipment updated =
                    equipmentService.updateEquipment(
                            id,
                            companyId,
                            categoryId,
                            item
                    );

            return ResponseEntity.ok(
                    toResponse(updated)
            );

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Integer id,
            @RequestParam String status,
            HttpSession session
    ) {

        try {

            Integer companyId =
                    getCompanyId(session);

            equipment updated =
                    equipmentService.updateStatus(
                            id,
                            companyId,
                            status
                    );

            return ResponseEntity.ok(
                    toResponse(updated)
            );

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateEquipment(
            @PathVariable Integer id,
            HttpSession session
    ) {

        try {

            Integer companyId =
                    getCompanyId(session);

            equipment updated =
                    equipmentService.deactivateEquipment(
                            id,
                            companyId
                    );

            return ResponseEntity.ok(
                    toResponse(updated)
            );

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }
}