package com.compulin.rentflow.service.module1;

import com.compulin.rentflow.entity.module1.company;
import com.compulin.rentflow.entity.module1.equipment;
import com.compulin.rentflow.entity.module1.equipment_category;
import com.compulin.rentflow.repository.module1.CompanyRepo;
import com.compulin.rentflow.repository.module1.equipment_category_repo;
import com.compulin.rentflow.repository.module1.equipment_repo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class equipment_service {

    private final equipment_repo equipmentRepo;
    private final CompanyRepo companyRepo;
    private final equipment_category_repo categoryRepo;

    public equipment_service(
            equipment_repo equipmentRepo,
            CompanyRepo companyRepo,
            equipment_category_repo categoryRepo
    ) {
        this.equipmentRepo = equipmentRepo;
        this.companyRepo = companyRepo;
        this.categoryRepo = categoryRepo;
    }

    public List<equipment> getEquipmentByCompany(
            Integer companyId
    ) {
        return equipmentRepo.findByCompanyId(companyId);
    }

    public equipment getEquipmentById(
            Integer equipmentId,
            Integer companyId
    ) {
        return equipmentRepo.findByIdAndCompanyId(
                equipmentId,
                companyId
        ).orElseThrow(() ->
                new RuntimeException(
                        "Equipment not found"
                )
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

        company company =
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
                                "Category does not belong to this company"
                        )
                );

        if (newEquipment.getItem_name() == null ||
                newEquipment.getItem_name().isBlank()) {

            throw new RuntimeException(
                    "Item name is required"
            );
        }

        if (newEquipment.getRental_rate() == null ||
                newEquipment.getRental_rate().signum() < 0) {

            throw new RuntimeException(
                    "Rental rate must be zero or greater"
            );
        }

        if (newEquipment.getRefundable_deposit_per_unit() == null ||
                newEquipment
                        .getRefundable_deposit_per_unit()
                        .signum() < 0) {

            throw new RuntimeException(
                    "Refundable deposit must be zero or greater"
            );
        }

        if (newEquipment.getTotal_quantity() == null ||
                newEquipment.getTotal_quantity() < 0) {

            throw new RuntimeException(
                    "Total quantity must be zero or greater"
            );
        }

        newEquipment.setCompany(company);
        newEquipment.setCategory(category);

        newEquipment.setAvailable_quantity(
                newEquipment.getTotal_quantity()
        );

        if (newEquipment.getEqu_status() == null ||
                newEquipment.getEqu_status().isBlank()) {

            newEquipment.setEqu_status("AVAILABLE");
        }

        if (newEquipment.getCreated_at() == null) {
            newEquipment.setCreated_at(
                    LocalDateTime.now()
            );
        }

        return equipmentRepo.save(newEquipment);
    }

    public equipment updateEquipment(
            Integer equipmentId,
            Integer companyId,
            Integer categoryId,
            equipment details
    ) {

        equipment existing =
                getEquipmentById(
                        equipmentId,
                        companyId
                );

        if (categoryId != null) {

            equipment_category category =
                    categoryRepo.findByIdAndCompanyId(
                            categoryId,
                            companyId
                    ).orElseThrow(() ->
                            new RuntimeException(
                                    "Category does not belong to this company"
                            )
                    );

            existing.setCategory(category);
        }

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

        existing.setRefundable_deposit_per_unit(
                details.getRefundable_deposit_per_unit()
        );

        Integer total =
                details.getTotal_quantity();

        Integer available =
                details.getAvailable_quantity();

        if (total == null || total < 0) {
            throw new RuntimeException(
                    "Total quantity must be zero or greater"
            );
        }

        if (available == null ||
                available < 0 ||
                available > total) {

            throw new RuntimeException(
                    "Available quantity must be between zero and total quantity"
            );
        }

        existing.setTotal_quantity(total);
        existing.setAvailable_quantity(available);

        if (details.getEqu_status() != null &&
                !details.getEqu_status().isBlank()) {

            existing.setEqu_status(
                    details.getEqu_status()
            );
        }

        return equipmentRepo.save(existing);
    }

    public equipment updateStatus(
            Integer equipmentId,
            Integer companyId,
            String status
    ) {

        if (status == null || status.isBlank()) {
            throw new RuntimeException(
                    "Status is required"
            );
        }

        equipment existing =
                getEquipmentById(
                        equipmentId,
                        companyId
                );

        existing.setEqu_status(status);

        return equipmentRepo.save(existing);
    }

    public void deactivateEquipment(
            Integer equipmentId,
            Integer companyId
    ) {

        equipment existing =
                getEquipmentById(
                        equipmentId,
                        companyId
                );

        existing.setEqu_status("INACTIVE");

        equipmentRepo.save(existing);
    }
}