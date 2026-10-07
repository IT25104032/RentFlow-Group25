package com.compulin.rentflow.repository.module1;

import com.compulin.rentflow.entity.module1.equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface equipment_repo extends JpaRepository<equipment, Integer> {

    @Query("""
        select e from equipment e
        where e.company.companyId = :companyId
        and lower(e.itemName) like lower(concat('%', :name, '%'))
    """)
    List<equipment> searchByCompany(
            @Param("companyId") Integer companyId,
            @Param("name") String name
    );

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
}