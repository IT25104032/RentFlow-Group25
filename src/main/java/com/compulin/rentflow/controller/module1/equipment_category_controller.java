package com.compulin.rentflow.controller.module1;

import com.compulin.rentflow.entity.module1.equipment_category;
import com.compulin.rentflow.repository.module1.equipment_category_repo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/module1/equipment-categories")
public class equipment_category_controller {

    private final equipment_category_repo categoryRepo;

    public equipment_category_controller(
            equipment_category_repo categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    // Get all categories
    @GetMapping
    public List<equipment_category> getAllCategories() {
        return categoryRepo.findAll();
    }

    // Get category by ID
    @GetMapping("/{id}")
    public ResponseEntity<equipment_category> getCategoryById(
            @PathVariable Integer id) {

        Optional<equipment_category> category =
                categoryRepo.findById(id);

        if (category.isPresent()) {
            return ResponseEntity.ok(category.get());
        }

        return ResponseEntity.notFound().build();
    }

    // Search categories by name
    @GetMapping("/search")
    public List<equipment_category> searchCategories(
            @RequestParam String name) {

        return categoryRepo
                .findByCategoryNameContainingIgnoreCase(name);
    }

    // Get categories by company
    @GetMapping("/company/{companyId}")
    public List<equipment_category> getCategoriesByCompany(
            @PathVariable Integer companyId) {

        return categoryRepo.findByCompanyCompanyId(companyId);
    }

    // Get categories by status
    @GetMapping("/status/{status}")
    public List<equipment_category> getCategoriesByStatus(
            @PathVariable String status) {

        return categoryRepo.findByCatStatus(status);
    }

    // Create category
    @PostMapping
    public equipment_category createCategory(
            @RequestBody equipment_category category) {

        return categoryRepo.save(category);
    }

    // Update category
    @PutMapping("/{id}")
    public ResponseEntity<equipment_category> updateCategory(
            @PathVariable Integer id,
            @RequestBody equipment_category categoryDetails) {

        Optional<equipment_category> existingCategory =
                categoryRepo.findById(id);

        if (existingCategory.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        equipment_category existing =
                existingCategory.get();

        existing.setCategoryName(
                categoryDetails.getCategoryName());

        existing.setCatDescription(
                categoryDetails.getCatDescription());

        existing.setCat_status(
                categoryDetails.getCat_status());

        if (categoryDetails.getCompany() != null) {
            existing.setCompany(
                    categoryDetails.getCompany());
        }

        return ResponseEntity.ok(
                categoryRepo.save(existing));
    }

    // Delete category
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Integer id) {

        if (!categoryRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        categoryRepo.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}