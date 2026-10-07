package com.compulin.rentflow.repository.module2;

import com.compulin.rentflow.entity.module2.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RentalRepository
        extends JpaRepository<Rental, Integer> {

    Optional<Rental> findByRentalIdAndCompanyId(
            Integer rentalId,
            Integer companyId
    );

    List<Rental> findByCustomerIdAndCompanyId(
            Integer customerId,
            Integer companyId
    );

    List<Rental> findByCompanyIdOrderByDueDateAsc(
            Integer companyId
    );

    @Query("""
        SELECT r
        FROM Rental r
        WHERE r.companyId = :companyId
          AND r.rentalStatus IN (
              'ACTIVE',
              'PARTIALLY_RETURNED'
          )
        ORDER BY r.dueDate
    """)
    List<Rental> findActiveRentals(
            @Param("companyId") Integer companyId
    );

    @Query("""
        SELECT r
        FROM Rental r
        WHERE r.companyId = :companyId
          AND r.rentalStatus = 'DRAFT'
        ORDER BY r.startDate, r.rentalId
    """)
    List<Rental> findRentalsForIssue(
            @Param("companyId") Integer companyId
    );

    @Query("""
        SELECT r
        FROM Rental r
        WHERE r.companyId = :companyId
          AND r.dueDate < CURRENT_DATE
          AND r.rentalStatus IN (
              'ACTIVE',
              'PARTIALLY_RETURNED',
              'OVERDUE'
          )
        ORDER BY r.dueDate
    """)
    List<Rental> findOverdueRentals(
            @Param("companyId") Integer companyId
    );

    @Query("""
        SELECT r
        FROM Rental r
        WHERE r.companyId = :companyId
          AND r.dueDate BETWEEN :today AND :dueSoonDate
          AND r.rentalStatus IN (
              'ACTIVE',
              'PARTIALLY_RETURNED'
          )
        ORDER BY r.dueDate
    """)
    List<Rental> findRentalsDueSoon(
            @Param("companyId") Integer companyId,
            @Param("today") LocalDate today,
            @Param("dueSoonDate") LocalDate dueSoonDate
    );
}
