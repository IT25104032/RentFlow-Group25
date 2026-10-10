package com.compulin.rentflow.service.module1;

import com.compulin.rentflow.entity.module1.company;
import com.compulin.rentflow.entity.module1.equipment_category;
import com.compulin.rentflow.repository.module1.CompanyRepo;
import com.compulin.rentflow.repository.module1.equipment_category_repo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class equipment_category_service {

    private final equipment_category_repo categoryRepo;
    private final CompanyRepo companyRepo;

    public equipment_category_service(
            equipment_category_repo categoryRepo,
            CompanyRepo companyRepo
    ) {
        this.categoryRepo = categoryRepo;
        this.companyRepo = companyRepo;
    }

    public List<equipment_category> getCategoriesByCompany(
            Integer companyId
    ) {
        return categoryRepo.findByCompanyId(companyId);
    }

    public Optional<equipment_category> getCategoryById(
            Integer categoryId,
            Integer companyId
    ) {
        return categoryRepo.findByIdAndCompanyId(
                categoryId,
                companyId
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
            Integer companyId,
            String categoryName,
            String description
    ) {

        if (categoryName == null ||
                categoryName.trim().isEmpty()) {

            throw new RuntimeException(
                    "Category name is required"
            );
        }

        company existingCompany =
                companyRepo.findById(companyId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Company not found"
                                )
                        );

        equipment_category category =
                new equipment_category();

        category.setCategoryName(
                categoryName.trim()
        );

        category.setCatDescription(
                description
        );

        category.setCat_status(
                "ACTIVE"
        );

        category.setCompany(
                existingCompany
        );

        return categoryRepo.save(category);
    }

    public equipment_category updateCategory(
            Integer categoryId,
            Integer companyId,
            String categoryName,
            String description,
            String status
    ) {

        equipment_category existing =
                categoryRepo.findByIdAndCompanyId(
                        categoryId,
                        companyId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Equipment category not found"
                        )
                );

        if (categoryName == null ||
                categoryName.trim().isEmpty()) {

            throw new RuntimeException(
                    "Category name is required"
            );
        }

        existing.setCategoryName(
                categoryName.trim()
        );

        existing.setCatDescription(
                description
        );

        if (status != null &&
                !status.trim().isEmpty()) {

            existing.setCat_status(
                    status.trim().toUpperCase()
            );
        }

        return categoryRepo.save(existing);
    }

    public equipment_category updateStatus(
            Integer categoryId,
            Integer companyId,
            String status
    ) {

        if (status == null ||
                status.trim().isEmpty()) {

            throw new RuntimeException(
                    "Status is required"
            );
        }

        String newStatus =
                status.trim().toUpperCase();

        if (!newStatus.equals("ACTIVE") &&
                !newStatus.equals("INACTIVE")) {

            throw new RuntimeException(
                    "Invalid category status"
            );
        }

        equipment_category existing =
                categoryRepo.findByIdAndCompanyId(
                        categoryId,
                        companyId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Equipment category not found"
                        )
                );

        existing.setCat_status(newStatus);

        return categoryRepo.save(existing);
    }

    public equipment_category deactivateCategory(
            Integer categoryId,
            Integer companyId
    ) {
        return updateStatus(
                categoryId,
                companyId,
                "INACTIVE"
        );
    }

    public equipment_category activateCategory(
            Integer categoryId,
            Integer companyId
    ) {
        return updateStatus(
                categoryId,
                companyId,
                "ACTIVE"
        );
    }
}