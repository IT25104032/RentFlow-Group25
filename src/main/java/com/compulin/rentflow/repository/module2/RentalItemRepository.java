package com.compulin.rentflow.repository.module2;

import com.compulin.rentflow.entity.module2.RentalItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface RentalItemRepository
        extends JpaRepository<RentalItem, Integer> {


    List<RentalItem> findByRentalId(
            Integer rentalId
    );


    Optional<RentalItem> findByRentalItemIdAndRentalId(
            Integer rentalItemId,
            Integer rentalId
    );
}