
package com.compulin.rentflow.controller.module1;

import com.compulin.rentflow.entity.module1.equipment;
import com.compulin.rentflow.repository.module1.equipment_repo;
import com.compulin.rentflow.security.Module1SessionAccess;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/module1/equipment-availability")
public class EquipmentAvailabilityController {

    private final equipment_repo equipmentRepo;

    public EquipmentAvailabilityController(
            equipment_repo equipmentRepo) {
        this.equipmentRepo = equipmentRepo;
    }

    @GetMapping("/available-for-rental")
    public ResponseEntity<List<equipment>> getAvailableEquipment(
            HttpSession session) {

        Integer companyId =
                Module1SessionAccess.getCompanyId(session);

        if (companyId == null) {
            return ResponseEntity.status(401).build();
        }

        if (!Module1SessionAccess.hasAnyRole(
                session, "COMPANY_ADMIN", "RENTAL_OFFICER")) {
            return ResponseEntity.status(403).build();
        }

        List<equipment> availableEquipment =
                equipmentRepo.findAvailableForRentalByCompany(
                        companyId);

        return ResponseEntity.ok(availableEquipment);
    }
}
