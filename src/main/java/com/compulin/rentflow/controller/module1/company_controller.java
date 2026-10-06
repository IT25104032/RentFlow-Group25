package com.compulin.rentflow.controller.module1;

import com.compulin.rentflow.entity.module1.company;
import com.compulin.rentflow.entity.module1.sys_user;
import com.compulin.rentflow.repository.module1.CompanyRepo;
import com.compulin.rentflow.repository.module1.sys_user_repo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/module1/companies")
public class company_controller {

    private final CompanyRepo companyRepo;
    private final sys_user_repo userRepo;

    public company_controller(
            CompanyRepo companyRepo,
            sys_user_repo userRepo) {

        this.companyRepo = companyRepo;
        this.userRepo = userRepo;
    }

    // Get all companies
    @GetMapping
    public List<company> getAllCompanies() {
        return companyRepo.findAll();
    }

    // Get company by ID
    @GetMapping("/{id}")
    public ResponseEntity<company> getCompanyById(
            @PathVariable Integer id) {

        Optional<company> existingCompany =
                companyRepo.findById(id);

        if (existingCompany.isPresent()) {
            return ResponseEntity.ok(existingCompany.get());
        }

        return ResponseEntity.notFound().build();
    }

    // Search company by name
    @GetMapping("/search")
    public List<company> searchCompanies(
            @RequestParam String name) {

        return companyRepo
                .findByCompanyNameContainingIgnoreCase(name);
    }

    // Get companies by status
    @GetMapping("/status/{status}")
    public List<company> getCompaniesByStatus(
            @PathVariable String status) {

        return companyRepo.findByCompanyStatus(status);
    }

    // Create company
    @PostMapping
    public ResponseEntity<?> createCompany(
            @RequestBody company newCompany) {

        try {

            // Set registration date automatically
            if (newCompany.getRegistration_date() == null) {
                newCompany.setRegistration_date(
                        LocalDateTime.now()
                );
            }

            // Set default status
            if (newCompany.getCompany_status() == null ||
                    newCompany.getCompany_status().isBlank()) {

                newCompany.setCompany_status("ACTIVE");
            }

            // Validate registered user
            if (newCompany.getRegistered_by() == null ||
                    newCompany.getRegistered_by().getUser_id() == null) {

                return ResponseEntity.badRequest()
                        .body("Registered By User ID is required.");
            }

            Integer userId =
                    newCompany.getRegistered_by().getUser_id();

            Optional<sys_user> user =
                    userRepo.findById(userId);

            if (user.isEmpty()) {

                return ResponseEntity.badRequest()
                        .body(
                                "User ID " + userId +
                                        " does not exist."
                        );
            }

            // Use the real user entity from database
            newCompany.setRegistered_by(user.get());

            company savedCompany =
                    companyRepo.save(newCompany);

            return ResponseEntity.ok(savedCompany);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(
                            "Could not create company: "
                                    + e.getMessage()
                    );
        }
    }

    // Update company
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCompany(
            @PathVariable Integer id,
            @RequestBody company companyDetails) {

        try {

            Optional<company> existingCompany =
                    companyRepo.findById(id);

            if (existingCompany.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            company existing =
                    existingCompany.get();

            // Update normal fields
            existing.setCompany_name(
                    companyDetails.getCompany_name()
            );

            existing.setRegistration_no(
                    companyDetails.getRegistration_no()
            );

            existing.setEmail(
                    companyDetails.getEmail()
            );

            existing.setPhone(
                    companyDetails.getPhone()
            );

            existing.setAddress(
                    companyDetails.getAddress()
            );

            existing.setCompany_status(
                    companyDetails.getCompany_status()
            );

            // IMPORTANT:
            // Keep the original registration date.
            // Do NOT replace it with null.
            if (companyDetails.getRegistration_date() != null) {

                existing.setRegistration_date(
                        companyDetails.getRegistration_date()
                );
            }

            // Update registered-by user only if supplied
            if (companyDetails.getRegistered_by() != null &&
                    companyDetails.getRegistered_by().getUser_id() != null) {

                Integer userId =
                        companyDetails
                                .getRegistered_by()
                                .getUser_id();

                Optional<sys_user> user =
                        userRepo.findById(userId);

                if (user.isEmpty()) {

                    return ResponseEntity.badRequest()
                            .body(
                                    "User ID " + userId +
                                            " does not exist."
                            );
                }

                existing.setRegistered_by(user.get());
            }

            company updatedCompany =
                    companyRepo.save(existing);

            return ResponseEntity.ok(updatedCompany);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(
                            "Could not update company: "
                                    + e.getMessage()
                    );
        }
    }

    // Delete company
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCompany(
            @PathVariable Integer id) {

        try {

            if (!companyRepo.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            companyRepo.deleteById(id);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(
                            "Could not delete company: "
                                    + e.getMessage()
                    );
        }
    }
}