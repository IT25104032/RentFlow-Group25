package com.compulin.rentflow.service.module1;

import com.compulin.rentflow.entity.module1.equipment_category;
import com.compulin.rentflow.repository.module1.equipment_category_repo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class equipment_category_service {

    private final equipment_category_repo categoryRepo;

    public equipment_category_service(
            equipment_category_repo categoryRepo
    ) {
        this.categoryRepo = categoryRepo;
    }

    public List<equipment_category> getCategoriesByCompany(
            Integer companyId
    ) {
        return categoryRepo.findByCompanyId(companyId);
    }

    public equipment_category getCategoryById(
            Integer categoryId,
            Integer companyId
    ) {
        return categoryRepo.findByIdAndCompanyId(
                categoryId,
                companyId
        ).orElseThrow(() ->
                new RuntimeException(
                        "Category not found"
                )
        );
    }

    public List<equipment_category> searchCategories(
            Integer companyId,
            String name
    ) {
        return categoryRepo.searchByCompany(
                companyId,
                name
        );
    }

    public List<equipment_category> getCategoriesByStatus(
            Integer companyId,
            String status
    ) {
        return categoryRepo.findByCompanyIdAndStatus(
                companyId,
                status
        );
    }

    public equipment_category createCategory(
            equipment_category newCategory
    ) {

        if (newCategory.getCategoryName() == null ||
                newCategory.getCategoryName().isBlank()) {

            throw new RuntimeException(
                    "Category name is required"
            );
        }

        if (newCategory.getCat_status() == null ||
                newCategory.getCat_status().isBlank()) {

            newCategory.setCat_status("ACTIVE");
        }

        return categoryRepo.save(newCategory);
    }

    public equipment_category updateCategory(
            Integer categoryId,
            Integer companyId,
            equipment_category categoryDetails
    ) {

        equipment_category existing =
                categoryRepo.findByIdAndCompanyId(
                        categoryId,
                        companyId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Category not found"
                        )
                );

        if (categoryDetails.getCategoryName() != null &&
                !categoryDetails.getCategoryName().isBlank()) {

            existing.setCategoryName(
                    categoryDetails.getCategoryName()
            );
        }

        existing.setCatDescription(
                categoryDetails.getCatDescription()
        );

        if (categoryDetails.getCat_status() != null &&
                !categoryDetails.getCat_status().isBlank()) {

            existing.setCat_status(
                    categoryDetails.getCat_status()
            );
        }

        return categoryRepo.save(existing);
    }

    public equipment_category updateStatus(
            Integer categoryId,
            Integer companyId,
            String status
    ) {

        equipment_category existing =
                categoryRepo.findByIdAndCompanyId(
                        categoryId,
                        companyId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Category not found"
                        )
                );

        existing.setCat_status(status);

        return categoryRepo.save(existing);
    }

    public void deleteCategory(
            Integer categoryId,
            Integer companyId
    ) {

        equipment_category existing =
                categoryRepo.findByIdAndCompanyId(
                        categoryId,
                        companyId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Category not found"
                        )
                );

        existing.setCat_status("INACTIVE");

        categoryRepo.save(existing);
    }
}