package com.compulin.rentflow.repository.module4;

import com.compulin.rentflow.entity.module4.RentalReturn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RentalReturnRepository
        extends JpaRepository<RentalReturn, Integer> {

    List<RentalReturn> findByRentalRentalId(Integer rentalId);
}

