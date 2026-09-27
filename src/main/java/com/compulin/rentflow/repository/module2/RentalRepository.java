package com.compulin.rentflow.repository.module2;


import com.compulin.rentflow.entity.module2.Rental;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RentalRepository extends JpaRepository<Rental, Integer> {

    Optional<Rental> findByRentalIdAndCompanyId(
            Integer rentalId,
            Integer companyId
    );

    List<Rental> findByCustomerIdAndCompanyId(
            Integer customerId,
            Integer companyId
    );
}