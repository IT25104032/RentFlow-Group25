package com.compulin.rentflow.controller.module1;

import com.compulin.rentflow.entity.module1.equipment;
import com.compulin.rentflow.repository.module1.equipment_repo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/module1/equipment")
public class equipment_controller {

    private final equipment_repo equipmentRepo;

    public equipment_controller(equipment_repo equipmentRepo) {
        this.equipmentRepo = equipmentRepo;
    }

    // Get all equipment
    @GetMapping
    public List<equipment> getAllEquipment() {
        return equipmentRepo.findAll();
    }

    // Get equipment by ID
    @GetMapping("/{id}")
    public ResponseEntity<equipment> getEquipmentById(
            @PathVariable Integer id) {

        Optional<equipment> existingEquipment =
                equipmentRepo.findById(id);

        if (existingEquipment.isPresent()) {
            return ResponseEntity.ok(existingEquipment.get());
        }

        return ResponseEntity.notFound().build();
    }

    // Search equipment by name
    @GetMapping("/search")
    public List<equipment> searchEquipment(
            @RequestParam String name) {

        return equipmentRepo
                .findByItemNameContainingIgnoreCase(name);
    }

    // Get equipment by company
    @GetMapping("/company/{companyId}")
    public List<equipment> getEquipmentByCompany(
            @PathVariable Integer companyId) {

        return equipmentRepo.findByCompanyCompanyId(companyId);
    }

    // Get equipment by category
    @GetMapping("/category/{categoryId}")
    public List<equipment> getEquipmentByCategory(
            @PathVariable Integer categoryId) {

        return equipmentRepo.findByCategoryCategoryId(categoryId);
    }

    // Get equipment by status
    @GetMapping("/status/{status}")
    public List<equipment> getEquipmentByStatus(
            @PathVariable String status) {

        return equipmentRepo.findByEquStatus(status);
    }

    // Create equipment
    @PostMapping
    public equipment createEquipment(
            @RequestBody equipment newEquipment) {

        return equipmentRepo.save(newEquipment);
    }

    // Update equipment
    @PutMapping("/{id}")
    public ResponseEntity<equipment> updateEquipment(
            @PathVariable Integer id,
            @RequestBody equipment equipmentDetails) {

        Optional<equipment> existingEquipment =
                equipmentRepo.findById(id);

        if (existingEquipment.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        equipment existing =
                existingEquipment.get();

        existing.setCompany(
                equipmentDetails.getCompany());

        existing.setCategory(
                equipmentDetails.getCategory());

        existing.setItem_name(
                equipmentDetails.getItem_name());

        existing.setItem_code(
                equipmentDetails.getItem_code());

        existing.setEqu_description(
                equipmentDetails.getEqu_description());

        existing.setRental_rate(
                equipmentDetails.getRental_rate());

        existing.setRate_period(
                equipmentDetails.getRate_period());

        existing.setRefundable_deposit_per_unit(
                equipmentDetails
                        .getRefundable_deposit_per_unit());

        existing.setTotal_quantity(
                equipmentDetails.getTotal_quantity());

        existing.setAvailable_quantity(
                equipmentDetails.getAvailable_quantity());

        existing.setEqu_status(
                equipmentDetails.getEqu_status());

        existing.setCreated_at(
                equipmentDetails.getCreated_at());

        return ResponseEntity.ok(
                equipmentRepo.save(existing));
    }

    // Delete equipment
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipment(
            @PathVariable Integer id) {

        if (!equipmentRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        equipmentRepo.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}