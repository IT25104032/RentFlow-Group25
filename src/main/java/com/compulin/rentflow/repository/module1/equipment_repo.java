//equipment
package com.compulin.rentflow.repository.module1;

import com.compulin.rentflow.entity.module1.equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface equipment_repo extends JpaRepository<equipment, Integer> {

    @Query("""
        select e from equipment e
        where e.company.companyId = :companyId
    """)
    List<equipment> findByCompanyId(
            @Param("companyId") Integer companyId
    );

    @Query("""
        select e from equipment e
        where e.equipmentId = :equipmentId
        and e.company.companyId = :companyId
    """)
    Optional<equipment> findByIdAndCompanyId(
            @Param("equipmentId") Integer equipmentId,
            @Param("companyId") Integer companyId
    );

    @Query("""
        select e from equipment e
        where e.category.categoryId = :categoryId
        and e.company.companyId = :companyId
    """)
    List<equipment> findByCategoryAndCompanyId(
            @Param("categoryId") Integer categoryId,
            @Param("companyId") Integer companyId
    );

    @Query("""
        select e from equipment e
        where e.company.companyId = :companyId
        and lower(e.equStatus) = lower(:status)
    """)
    List<equipment> findByCompanyIdAndStatus(
            @Param("companyId") Integer companyId,
            @Param("status") String status
    );

    @Query("""
        select e from equipment e
        where e.company.companyId = :companyId
        and lower(e.itemName) like lower(concat('%', :name, '%'))
    """)
    List<equipment> searchByCompany(
            @Param("companyId") Integer companyId,
            @Param("name") String name
    );


    @Query(
            value = "SELECT * FROM equipment " +
                    "WHERE company_id = :companyId " +
                    "ORDER BY item_name",
            nativeQuery = true
    )
    List<equipment> findAllForCompany(
            @Param("companyId") Integer companyId
    );

    @Query(
            value = "SELECT * FROM equipment " +
                    "WHERE equipment_id = :equipmentId " +
                    "AND company_id = :companyId",
            nativeQuery = true
    )
    Optional<equipment> findOwnedEquipment(
            @Param("equipmentId") Integer equipmentId,
            @Param("companyId") Integer companyId
    );


    @Query("""
    select e from equipment e
    where e.company.companyId = :companyId
      and upper(e.equStatus) = 'ACTIVE'
      and e.availableQuantity > 0
    order by e.itemName
    """)
    List<equipment> findAvailableForRentalByCompany(
            @Param("companyId") Integer companyId
    );


    @Modifying
    @Query(value = """
    UPDATE equipment
    SET available_quantity = available_quantity - :quantity
    WHERE equipment_id = :equipmentId
      AND company_id = :companyId
      AND UPPER(equ_status) = 'ACTIVE'
      AND :quantity > 0
      AND available_quantity >= :quantity
    """, nativeQuery = true)
    int reserveQuantityForIssue(
            @Param("equipmentId") Integer equipmentId,
            @Param("companyId") Integer companyId,
            @Param("quantity") Integer quantity
    );

    @Query(value = """
    SELECT * FROM equipment
    WHERE company_id = :companyId
    ORDER BY item_name
    """, nativeQuery = true)
    List<equipment> findModule1EquipmentByCompany(
            @Param("companyId") Integer companyId
    );

    @Query(value = """
    SELECT * FROM equipment
    WHERE equipment_id = :equipmentId
      AND company_id = :companyId   
    """, nativeQuery = true)
    Optional<equipment> findModule1EquipmentOwnedByCompany(
            @Param("equipmentId") Integer equipmentId,
            @Param("companyId") Integer companyId
    );

    @Query(value = """
    SELECT * FROM equipment
    WHERE company_id = :companyId
      AND LOWER(item_name) LIKE CONCAT('%', LOWER(:name), '%')
    ORDER BY item_name
    """, nativeQuery = true)
    List<equipment> searchModule1EquipmentByCompany(
            @Param("companyId") Integer companyId,
            @Param("name") String name
    );

    @Query(value = """
    SELECT * FROM equipment
    WHERE company_id = :companyId
      AND category_id = :categoryId
    ORDER BY item_name
    """, nativeQuery = true)
    List<equipment> findModule1EquipmentByCategoryAndCompany(
            @Param("companyId") Integer companyId,
            @Param("categoryId") Integer categoryId
    );

    @Query(value = """
    SELECT * FROM equipment
    WHERE company_id = :companyId
      AND UPPER(equ_status) = UPPER(:status)
    ORDER BY item_name
    """, nativeQuery = true)
    List<equipment> findModule1EquipmentByStatusAndCompany(
            @Param("companyId") Integer companyId,
            @Param("status") String status
    );




    @Modifying
    @Query(value = """
        UPDATE equipment
        SET available_quantity = available_quantity + :quantity
        WHERE equipment_id = :equipmentId
          AND company_id = :companyId
          AND :quantity > 0
          AND available_quantity + :quantity <= total_quantity
        """, nativeQuery = true)
    int restoreReservedQuantity(
            @Param("equipmentId") Integer equipmentId,
            @Param("companyId") Integer companyId,
            @Param("quantity") Integer quantity);
}