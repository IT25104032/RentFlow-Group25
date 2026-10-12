package com.compulin.rentflow.service.module1;

import com.compulin.rentflow.repository.module1.equipment_repo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EquipmentAvailabilityService {

    private final equipment_repo equipmentRepo;

    public EquipmentAvailabilityService(
            equipment_repo equipmentRepo) {
        this.equipmentRepo = equipmentRepo;
    }

    @Transactional
    public void reserveForIssue(
            Integer companyId,
            Integer equipmentId,
            Integer quantity) {

        if (companyId == null
                || equipmentId == null
                || quantity == null
                || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Company, equipment and a positive quantity are required."
            );
        }

        int updatedRows =
                equipmentRepo.reserveQuantityForIssue(
                        equipmentId,
                        companyId,
                        quantity
                );

        if (updatedRows != 1) {
            throw new IllegalStateException(
                    "Equipment issue rejected: equipment is unavailable, " +
                            "inactive, or has insufficient quantity."
            );
        }
    }
}