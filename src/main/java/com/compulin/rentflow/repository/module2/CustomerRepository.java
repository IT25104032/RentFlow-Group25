package com.compulin.rentflow.repository.module2;

import com.compulin.rentflow.entity.module2.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    Optional<Customer> findByCustomerIdAndCompanyId(
            Integer customerId,
            Integer companyId
    );
}
