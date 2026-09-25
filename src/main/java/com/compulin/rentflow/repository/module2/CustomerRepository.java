package com.compulin.rentflow.repository.module2;

import com.compulin.rentflow.entity.module2.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
}
