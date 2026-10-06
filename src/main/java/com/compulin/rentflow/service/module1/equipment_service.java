package com.compulin.rentflow.service.module1;

import com.compulin.rentflow.entity.module1.equipment;
import com.compulin.rentflow.repository.module1.equipment_repo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class equipment_service {

    private final equipment_repo equipmentRepo;

    public equipment_service(equipment_repo equipmentRepo) {
        this.equipmentRepo = equipmentRepo;
    }

    // Get all equipment
    public List<equipment> getAllEquipment() {
        return equipmentRepo.findAll();
    }

    // Get equipment by ID
    public equipment getEquipmentById(Integer id) {
        return equipmentRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Equipment not found with ID: " + id));
    }

    // Search equipment by name
    public List<equipment> searchEquipment(String name) {
        return equipmentRepo
                .findByItemNameContainingIgnoreCase(name);
    }

    // Get equipment by company
    public List<equipment> getEquipmentByCompany(
            Integer companyId) {

        return equipmentRepo.findByCompanyCompanyId(companyId);
    }

    // Get equipment by category
    public List<equipment> getEquipmentByCategory(
            Integer categoryId) {

        return equipmentRepo.findByCategoryCategoryId(categoryId);
    }

    // Get equipment by status
    public List<equipment> getEquipmentByStatus(
            String status) {

        return equipmentRepo.findByEquStatus(status);
    }

    // Create equipment
    public equipment createEquipment(
            equipment newEquipment) {

        if (newEquipment.getCreated_at() == null) {
            newEquipment.setCreated_at(
                    LocalDateTime.now());
        }

        if (newEquipment.getEqu_status() == null ||
                newEquipment.getEqu_status().isBlank()) {

            newEquipment.setEqu_status("AVAILABLE");
        }

        if (newEquipment.getAvailable_quantity() == null &&
                newEquipment.getTotal_quantity() != null) {

            newEquipment.setAvailable_quantity(
                    newEquipment.getTotal_quantity());
        }

        return equipmentRepo.save(newEquipment);
    }

    // Update equipment
    public equipment updateEquipment(
            Integer id,
            equipment equipmentDetails) {

        equipment existingEquipment =
                getEquipmentById(id);

        if (equipmentDetails.getCompany() != null) {
            existingEquipment.setCompany(
                    equipmentDetails.getCompany());
        }

        if (equipmentDetails.getCategory() != null) {
            existingEquipment.setCategory(
                    equipmentDetails.getCategory());
        }

        if (equipmentDetails.getItem_name() != null) {
            existingEquipment.setItem_name(
                    equipmentDetails.getItem_name());
        }

        if (equipmentDetails.getItem_code() != null) {
            existingEquipment.setItem_code(
                    equipmentDetails.getItem_code());
        }

        if (equipmentDetails.getEqu_description() != null) {
            existingEquipment.setEqu_description(
                    equipmentDetails.getEqu_description());
        }

        if (equipmentDetails.getRental_rate() != null) {
            existingEquipment.setRental_rate(
                    equipmentDetails.getRental_rate());
        }

        if (equipmentDetails.getRate_period() != null) {
            existingEquipment.setRate_period(
                    equipmentDetails.getRate_period());
        }

        if (equipmentDetails
                .getRefundable_deposit_per_unit() != null) {

            existingEquipment.setRefundable_deposit_per_unit(
                    equipmentDetails
                            .getRefundable_deposit_per_unit());
        }

        if (equipmentDetails.getTotal_quantity() != null) {
            existingEquipment.setTotal_quantity(
                    equipmentDetails.getTotal_quantity());
        }

        if (equipmentDetails.getAvailable_quantity() != null) {
            existingEquipment.setAvailable_quantity(
                    equipmentDetails
                            .getAvailable_quantity());
        }

        if (equipmentDetails.getEqu_status() != null) {
            existingEquipment.setEqu_status(
                    equipmentDetails.getEqu_status());
        }

        return equipmentRepo.save(existingEquipment);
    }

    // Change equipment status
    public equipment updateEquipmentStatus(
            Integer id,
            String status) {

        equipment existingEquipment =
                getEquipmentById(id);

        existingEquipment.setEqu_status(status);

        return equipmentRepo.save(existingEquipment);
    }

    // Update available quantity
    public equipment updateAvailableQuantity(
            Integer id,
            Integer availableQuantity) {

        equipment existingEquipment =
                getEquipmentById(id);

        if (availableQuantity == null ||
                availableQuantity < 0 ||
                existingEquipment.getTotal_quantity() == null ||
                availableQuantity >
                        existingEquipment.getTotal_quantity()) {

            throw new IllegalArgumentException(
                    "Available quantity must be between 0 and total quantity.");
        }

        existingEquipment.setAvailable_quantity(
                availableQuantity);

        return equipmentRepo.save(existingEquipment);
    }

    // Delete equipment
    public void deleteEquipment(Integer id) {

        equipment existingEquipment =
                getEquipmentById(id);

        equipmentRepo.delete(existingEquipment);
    }
}
