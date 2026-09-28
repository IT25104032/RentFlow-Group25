package com.compulin.rentflow.repository.module3;

import com.compulin.rentflow.entity.module3.SecurityDeposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecurityDepositRepository extends JpaRepository<SecurityDeposit, Integer> {

    // Finds the deposit associated with a specific rental (rental_id is unique)
    Optional<SecurityDeposit> findByRentalId(Integer rentalId);

    // Finds deposits filtered by status (e.g., PENDING, HELD, REFUNDED)
    List<SecurityDeposit> findByDepositStatus(SecurityDeposit.DepositStatus depositStatus);

    // Finds deposits collected by a specific staff member
    List<SecurityDeposit> findByReceivedBy(Integer receivedBy);
}
