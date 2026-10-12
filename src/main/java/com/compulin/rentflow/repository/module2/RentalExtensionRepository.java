package com.compulin.rentflow.repository.module2;

import com.compulin.rentflow.entity.module2.RentalExtension;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RentalExtensionRepository
        extends JpaRepository<RentalExtension, Integer> {

    List<RentalExtension> findByRentalIdOrderByCreatedAtDesc(
            Integer rentalId
    );
}
