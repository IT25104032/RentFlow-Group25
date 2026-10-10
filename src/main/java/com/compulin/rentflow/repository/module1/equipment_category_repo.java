package com.compulin.rentflow.repository.module1;

import com.compulin.rentflow.entity.module1.equipment_category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;
import java.util.Optional;

public interface equipment_category_repo
        extends JpaRepository<equipment_category, Integer> {

    @Query("""
        select c from equipment_category c
        where c.companyId.companyId = :companyId
        and lower(c.categoryName) like lower(concat('%', :name, '%'))
    """)
    List<equipment_category> searchByCompany(
            @Param("companyId") Integer companyId,
            @Param("name") String name
    );

    @Query("""
        select c from equipment_category c
        where c.companyId.companyId = :companyId
    """)
    List<equipment_category> findByCompanyId(
            @Param("companyId") Integer companyId
    );

    @Query("""
        select c from equipment_category c
        where c.categoryId = :categoryId
        and c.companyId.companyId = :companyId
    """)
    Optional<equipment_category> findByIdAndCompanyId(
            @Param("categoryId") Integer categoryId,
            @Param("companyId") Integer companyId
    );

    @Query("""
        select c from equipment_category c
        where c.companyId.companyId = :companyId
        and lower(c.catStatus) = lower(:status)
    """)
    List<equipment_category> findByCompanyIdAndStatus(
            @Param("companyId") Integer companyId,
            @Param("status") String status
    );


    @Query(
            value = "SELECT * FROM equipment_category " +
                    "WHERE company_id = :companyId " +
                    "ORDER BY category_name",
            nativeQuery = true
    )
    List<equipment_category> findAllForCompany(
            @Param("companyId") Integer companyId
    );

    @Query(
            value = "SELECT * FROM equipment_category " +
                    "WHERE category_id = :categoryId " +
                    "AND company_id = :companyId",
            nativeQuery = true
    )
    Optional<equipment_category> findOwnedCategory(
            @Param("categoryId") Integer categoryId,
            @Param("companyId") Integer companyId
    );


}