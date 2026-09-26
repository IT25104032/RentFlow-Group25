package com.compulin.rentflow.repository.module2;

import com.compulin.rentflow.entity.module2.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    Optional<Customer> findByCustomerIdAndCompanyId(
            Integer customerId,
            Integer companyId
    );

    @Query("""
        SELECT c FROM Customer c
        WHERE c.companyId = :companyId
          AND (
              (:name IS NOT NULL AND
               LOWER(c.customerName) LIKE LOWER(CONCAT('%', :name, '%')))
              OR
              (:phone IS NOT NULL AND c.phone = :phone)
          )
        ORDER BY c.customerName
    """)
    List<Customer> searchCustomers(
            @Param("companyId") Integer companyId,
            @Param("name") String name,
            @Param("phone") String phone
    );
}
