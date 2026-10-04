package com.compulin.rentflow.service.module4;

import com.compulin.rentflow.entity.module4.DamageRecord;
import com.compulin.rentflow.repository.module4.DamageRecordRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DamageService {

    private final DamageRecordRepository damageRecordRepository;

    public DamageService(
            DamageRecordRepository damageRecordRepository) {

        this.damageRecordRepository = damageRecordRepository;
    }

    public List<DamageRecord> getAllDamageRecords() {
        return damageRecordRepository.findAll();
    }

    public DamageRecord getDamageRecord(
            Integer id) {

        return damageRecordRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Damage record not found"
                        )
                );
    }

    public DamageRecord createDamageRecord(
            DamageRecord damageRecord) {

        if (damageRecord.getReturnItem() == null ||
                damageRecord.getReturnItem()
                        .getReturnItemId() == null) {

            throw new IllegalArgumentException(
                    "Return item is required."
            );
        }

        if (damageRecord.getDamagedQuantity() == null ||
                damageRecord.getDamagedQuantity() <= 0) {

            throw new IllegalArgumentException(
                    "Damaged quantity must be greater than zero."
            );
        }

        if (damageRecord.getDamageDescription() == null ||
                damageRecord.getDamageDescription().isBlank()) {

            throw new IllegalArgumentException(
                    "Damage description is required."
            );
        }

        if (damageRecord.getDamageLevel() == null) {
            throw new IllegalArgumentException(
                    "Damage level is required."
            );
        }

        if (damageRecord.getEstimatedCost() == null) {
            damageRecord.setEstimatedCost(
                    BigDecimal.ZERO
            );
        }

        if (damageRecord.getFinalCharge() == null) {
            damageRecord.setFinalCharge(
                    BigDecimal.ZERO
            );
        }

        if (damageRecord.getAssessmentDate() == null) {
            damageRecord.setAssessmentDate(
                    LocalDateTime.now()
            );
        }

        if (damageRecord.getStatus() == null ||
                damageRecord.getStatus().isBlank()) {

            damageRecord.setStatus("ASSESSED");
        }

        return damageRecordRepository.save(
                damageRecord
        );
    }

    public DamageRecord updateDamageRecord(
            Integer id,
            DamageRecord newRecord) {

        DamageRecord record =
                getDamageRecord(id);

        record.setDamagedQuantity(
                newRecord.getDamagedQuantity()
        );

        record.setDamageDescription(
                newRecord.getDamageDescription()
        );

        record.setDamageLevel(
                newRecord.getDamageLevel()
        );

        record.setEstimatedCost(
                newRecord.getEstimatedCost()
        );

        record.setFinalCharge(
                newRecord.getFinalCharge()
        );

        record.setStatus(
                newRecord.getStatus()
        );

        return damageRecordRepository.save(
                record
        );
    }
}
