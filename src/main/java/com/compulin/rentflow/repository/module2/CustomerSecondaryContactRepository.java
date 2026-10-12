package com.compulin.rentflow.repository.module2;


import com.compulin.rentflow.entity.module2.CustomerSecondaryContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerSecondaryContactRepository
        extends JpaRepository<CustomerSecondaryContact, Integer> {

    Optional<CustomerSecondaryContact> findByCustomerId(
            Integer customerId
    );

    Optional<CustomerSecondaryContact> findBySecondaryContactIdAndCustomerId(
            Integer secondaryContactId,
            Integer customerId
    );
}
