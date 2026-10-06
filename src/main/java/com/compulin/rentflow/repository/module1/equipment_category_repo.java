package com.compulin.rentflow.repository.module1;

import com.compulin.rentflow.entity.module1.equipment_category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface equipment_category_repo
        extends JpaRepository<equipment_category, Integer> {

    List<equipment_category> findByCategoryNameContainingIgnoreCase(
            String categoryName);

    List<equipment_category> findByCatStatus(String catStatus);

    List<equipment_category> findByCompanyCompanyId(Integer companyId);
}
