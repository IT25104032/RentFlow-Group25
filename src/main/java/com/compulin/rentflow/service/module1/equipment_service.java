package com.compulin.rentflow.service.module1;

import com.compulin.rentflow.entity.module1.company;
import com.compulin.rentflow.entity.module1.equipment;
import com.compulin.rentflow.entity.module1.equipment_category;
import com.compulin.rentflow.repository.module1.CompanyRepo;
import com.compulin.rentflow.repository.module1.equipment_category_repo;
import com.compulin.rentflow.repository.module1.equipment_repo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class equipment_service {

    private final equipment_repo equipmentRepo;
    private final equipment_category_repo categoryRepo;
    private final CompanyRepo companyRepo;

    public equipment_service(
            equipment_repo equipmentRepo,
            equipment_category_repo categoryRepo,
            CompanyRepo companyRepo
    ) {
        this.equipmentRepo = equipmentRepo;
        this.categoryRepo = categoryRepo;
        this.companyRepo = companyRepo;
    }

    public List<equipment> getEquipmentByCompany(
            Integer companyId
    ) {
        return equipmentRepo.findByCompanyId(companyId);
    }

    public Optional<equipment> getEquipmentById(
            Integer equipmentId,
            Integer companyId
    ) {
        return equipmentRepo.findByIdAndCompanyId(
                equipmentId,
                companyId
        );
    }

    public List<equipment> searchEquipment(
            Integer companyId,
            String name
    ) {
        return equipmentRepo.searchByCompany(
                companyId,
                name
        );
    }

    public List<equipment> getEquipmentByCategory(
            Integer companyId,
            Integer categoryId
    ) {
        return equipmentRepo.findByCategoryAndCompanyId(
                categoryId,
                companyId
        );
    }

    public List<equipment> getEquipmentByStatus(
            Integer companyId,
            String status
    ) {
        return equipmentRepo.findByCompanyIdAndStatus(
                companyId,
                status
        );
    }

    public equipment createEquipment(
            Integer companyId,
            Integer categoryId,
            equipment newEquipment
    ) {

        validateEquipment(newEquipment);

        company existingCompany =
                companyRepo.findById(companyId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Company not found"
                                )
                        );

        equipment_category category =
                categoryRepo.findByIdAndCompanyId(
                        categoryId,
                        companyId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Equipment category not found"
                        )
                );

        newEquipment.setCompany(existingCompany);
        newEquipment.setCategory(category);

        newEquipment.setAvailable_quantity(
                newEquipment.getTotal_quantity()
        );

        newEquipment.setEqu_status(
                "AVAILABLE"
        );

        newEquipment.setCreated_at(
                LocalDateTime.now()
        );

        return equipmentRepo.save(newEquipment);
    }

    public equipment updateEquipment(
            Integer equipmentId,
            Integer companyId,
            Integer categoryId,
            equipment details
    ) {

        equipment existing =
                equipmentRepo.findByIdAndCompanyId(
                        equipmentId,
                        companyId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Equipment not found"
                        )
                );

        validateEquipment(details);

        equipment_category category =
                categoryRepo.findByIdAndCompanyId(
                        categoryId,
                        companyId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Equipment category not found"
                        )
                );

        if (details.getAvailable_quantity() < 0) {
            throw new RuntimeException(
                    "Available quantity cannot be negative"
            );
        }

        if (details.getAvailable_quantity()
                > details.getTotal_quantity()) {

            throw new RuntimeException(
                    "Available quantity cannot be greater than total quantity"
            );
        }

        existing.setCategory(category);

        existing.setItem_name(
                details.getItem_name()
        );

        existing.setItem_code(
                details.getItem_code()
        );

        existing.setEqu_description(
                details.getEqu_description()
        );

        existing.setRental_rate(
                details.getRental_rate()
        );

        existing.setRate_period(
                details.getRate_period()
        );

        existing.setSecurity_deposit_per_unit(
                details.getSecurity_deposit_per_unit()
        );

        existing.setTotal_quantity(
                details.getTotal_quantity()
        );

        existing.setAvailable_quantity(
                details.getAvailable_quantity()
        );

        if (details.getEqu_status() != null &&
                !details.getEqu_status().trim().isEmpty()) {

            existing.setEqu_status(
                    normalizeStatus(
                            details.getEqu_status()
                    )
            );
        }

        return equipmentRepo.save(existing);
    }

    public equipment updateStatus(
            Integer equipmentId,
            Integer companyId,
            String status
    ) {

        equipment existing =
                equipmentRepo.findByIdAndCompanyId(
                        equipmentId,
                        companyId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Equipment not found"
                        )
                );

        String newStatus =
                normalizeStatus(status);

        existing.setEqu_status(newStatus);

        if (newStatus.equals("INACTIVE") ||
                newStatus.equals("DAMAGED") ||
                newStatus.equals("LOST") ||
                newStatus.equals("MAINTENANCE")) {

            existing.setAvailable_quantity(0);
        }

        return equipmentRepo.save(existing);
    }

    public equipment deactivateEquipment(
            Integer equipmentId,
            Integer companyId
    ) {
        return updateStatus(
                equipmentId,
                companyId,
                "INACTIVE"
        );
    }

    private void validateEquipment(
            equipment item
    ) {

        if (item.getItem_name() == null ||
                item.getItem_name().trim().isEmpty()) {

            throw new RuntimeException(
                    "Item name is required"
            );
        }

        if (item.getRental_rate() == null ||
                item.getRental_rate()
                        .compareTo(BigDecimal.ZERO) < 0) {

            throw new RuntimeException(
                    "Rental rate must be zero or greater"
            );
        }

        if (item.getSecurity_deposit_per_unit() == null ||
                item.getSecurity_deposit_per_unit()
                        .compareTo(BigDecimal.ZERO) < 0) {

            throw new RuntimeException(
                    "Security deposit must be zero or greater"
            );
        }

        if (item.getTotal_quantity() == null ||
                item.getTotal_quantity() <= 0) {

            throw new RuntimeException(
                    "Total quantity must be greater than zero"
            );
        }

        if (item.getRate_period() == null ||
                item.getRate_period().trim().isEmpty()) {

            throw new RuntimeException(
                    "Rate period is required"
            );
        }
    }

    private String normalizeStatus(
            String status
    ) {

        if (status == null ||
                status.trim().isEmpty()) {

            throw new RuntimeException(
                    "Equipment status is required"
            );
        }

        String value =
                status.trim().toUpperCase();

        if (!value.equals("AVAILABLE") &&
                !value.equals("RENTED") &&
                !value.equals("DAMAGED") &&
                !value.equals("LOST") &&
                !value.equals("MAINTENANCE") &&
                !value.equals("INACTIVE")) {

            throw new RuntimeException(
                    "Invalid equipment status"
            );
        }

        return value;
    }
}