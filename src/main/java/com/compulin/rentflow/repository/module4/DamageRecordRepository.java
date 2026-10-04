package com.compulin.rentflow.repository.module4;

import com.compulin.rentflow.entity.module4.DamageRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DamageRecordRepository
        extends JpaRepository<DamageRecord, Integer> {

    List<DamageRecord> findByDamageLevel(String damageLevel);

    List<DamageRecord> findByStatus(String status);
}
