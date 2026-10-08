package com.compulin.rentflow.repository.module4;

import com.compulin.rentflow.entity.module4.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettlementRepository
        extends JpaRepository<Settlement, Integer> {

    Optional<Settlement> findByRentalId(Integer rentalId);
}
