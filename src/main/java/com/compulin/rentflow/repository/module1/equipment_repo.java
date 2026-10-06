package com.compulin.rentflow.repository.module1;

import com.compulin.rentflow.entity.module1.equipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface equipment_repo extends JpaRepository<equipment, Integer> {

    List<equipment> findByItemNameContainingIgnoreCase(
            String item_name);

    List<equipment> findByEquStatus(String equ_status);

    List<equipment> findByCompanyCompanyId(
            Integer company_id);

    List<equipment> findByCategoryCategoryId(
            Integer categoryId);
}