package com.compulin.rentflow.service.module1;

import com.compulin.rentflow.entity.module1.equipment_category;
import com.compulin.rentflow.repository.module1.equipment_category_repo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class equipment_category_service {

    private final equipment_category_repo categoryRepo;

    public equipment_category_service(
            equipment_category_repo categoryRepo) {

        this.categoryRepo = categoryRepo;
    }

    // Get all categories
    public List<equipment_category> getAllCategories() {
        return categoryRepo.findAll();
    }

    // Get category by ID
    public equipment_category getCategoryById(Integer id) {
        return categoryRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Category not found with ID: " + id));
    }

    // Search categories by name
    public List<equipment_category> searchCategories(
            String name) {

        return categoryRepo
                .findByCategoryNameContainingIgnoreCase(name);
    }

    // Get categories by status
    public List<equipment_category> getCategoriesByStatus(
            String status) {

        return categoryRepo.findByCatStatus(status);
    }

    // Get categories by company
    public List<equipment_category> getCategoriesByCompany(
            Integer companyId) {

        return categoryRepo.findByCompanyCompanyId(companyId);
    }

    // Create category
    public equipment_category createCategory(
            equipment_category newCategory) {

        if (newCategory.getCat_status() == null ||
                newCategory.getCat_status().isBlank()) {

            newCategory.setCat_status("ACTIVE");
        }

        return categoryRepo.save(newCategory);
    }

    // Update category
    public equipment_category updateCategory(
            Integer id,
            equipment_category categoryDetails) {

        equipment_category existingCategory =
                getCategoryById(id);

        if (categoryDetails.getCategoryName() != null) {
            existingCategory.setCategoryName(
                    categoryDetails.getCategoryName());
        }

        if (categoryDetails.getCatDescription() != null) {
            existingCategory.setCatDescription(
                    categoryDetails.getCatDescription());
        }

        if (categoryDetails.getCat_status() != null) {
            existingCategory.setCat_status(
                    categoryDetails.getCat_status());
        }

        if (categoryDetails.getCompany() != null) {
            existingCategory.setCompany(
                    categoryDetails.getCompany());
        }

        return categoryRepo.save(existingCategory);
    }

    // Delete category
    public void deleteCategory(Integer id) {

        equipment_category existingCategory =
                getCategoryById(id);

        categoryRepo.delete(existingCategory);
    }
}
