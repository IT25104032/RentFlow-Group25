package com.compulin.rentflow.service.module1;

import com.compulin.rentflow.dto.module1.CompanyRegistrationRequest;
import com.compulin.rentflow.entity.module1.company;
import com.compulin.rentflow.entity.module1.sys_user;
import com.compulin.rentflow.repository.module1.CompanyRepo;
import com.compulin.rentflow.repository.module1.sys_user_repo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class company_service {

    private final CompanyRepo companyRepo;
    private final sys_user_repo userRepo;
    private final PasswordEncoder passwordEncoder;

    public company_service(
            CompanyRepo companyRepo,
            sys_user_repo userRepo,
            PasswordEncoder passwordEncoder
    ) {
        this.companyRepo = companyRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public company registerCompany(
            CompanyRegistrationRequest request,
            Integer compulinAdminId
    ) {

        if (request.getCompanyName() == null ||
                request.getCompanyName().isBlank()) {
            throw new RuntimeException(
                    "Company name is required"
            );
        }

        if (request.getEmail() == null ||
                request.getEmail().isBlank()) {
            throw new RuntimeException(
                    "Company email is required"
            );
        }

        if (request.getAdminFullName() == null ||
                request.getAdminFullName().isBlank()) {
            throw new RuntimeException(
                    "Initial Company Admin name is required"
            );
        }

        if (request.getAdminPassword() == null ||
                request.getAdminPassword().isBlank()) {
            throw new RuntimeException(
                    "Initial Company Admin password is required"
            );
        }

        sys_user compulinAdmin =
                userRepo.findById(compulinAdminId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Compulin Administrator not found"
                                )
                        );

        if (!"COMPULIN_ADMIN".equalsIgnoreCase(
                compulinAdmin.getUser_role()
        )) {
            throw new RuntimeException(
                    "Only the Compulin Administrator can register a company"
            );
        }

        if (userRepo.findByEmail(
                request.getEmail()
        ).isPresent()) {
            throw new RuntimeException(
                    "This email is already registered"
            );
        }

        company newCompany = new company();

        newCompany.setCompany_name(
                request.getCompanyName()
        );

        newCompany.setRegistration_no(
                request.getRegistrationNo()
        );

        newCompany.setEmail(
                request.getEmail()
        );

        newCompany.setPhone(
                request.getPhone()
        );

        newCompany.setAddress(
                request.getAddress()
        );

        newCompany.setRegistered_by(
                compulinAdmin
        );

        newCompany.setRegistration_date(
                LocalDateTime.now()
        );

        newCompany.setCompany_status(
                "ACTIVE"
        );

        company savedCompany =
                companyRepo.save(newCompany);

        sys_user initialAdmin = new sys_user();

        initialAdmin.setCompany(
                savedCompany
        );

        initialAdmin.setFull_name(
                request.getAdminFullName()
        );

        initialAdmin.setEmail(
                request.getEmail()
        );

        initialAdmin.setPassword_hash(
                passwordEncoder.encode(
                        request.getAdminPassword()
                )
        );

        initialAdmin.setPhone(
                request.getPhone()
        );

        initialAdmin.setUser_role(
                "COMPANY_ADMIN"
        );

        initialAdmin.setUser_status(
                "ACTIVE"
        );

        initialAdmin.setCreated_at(
                LocalDateTime.now()
        );

        userRepo.save(initialAdmin);

        return savedCompany;
    }

    public company getCompanyById(Integer id) {
        return companyRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Company not found"
                        )
                );
    }

    @Transactional
    public company updateCompany(
            Integer id,
            company companyDetails
    ) {

        company existing =
                companyRepo.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Company not found"
                                )
                        );

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

        // Synchronize users only when the company actually changes status.
        // Editing the name, phone, etc. must not reset individual user statuses.
        String requestedStatus = companyDetails.getCompany_status();
        if (requestedStatus != null && !requestedStatus.isBlank()) {
            String normalized = requestedStatus.trim().toUpperCase(java.util.Locale.ROOT);
            if (!"ACTIVE".equals(normalized) && !"INACTIVE".equals(normalized)) {
                throw new IllegalArgumentException("Company status must be ACTIVE or INACTIVE");
            }
            if (!normalized.equalsIgnoreCase(existing.getCompany_status())) {
                setCompanyAndUsersStatus(existing, normalized);
            }
        }

        return companyRepo.save(existing);
    }

    @Transactional
    public void deactivateCompany(Integer id) {
        company existing = companyRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        setCompanyAndUsersStatus(existing, "INACTIVE");
        companyRepo.save(existing);
    }

    /**
     * Apply a company status change and synchronize ALL company users.
     * Deactivation and reactivation are atomic in the calling transaction.
     * COMPULIN_ADMIN accounts (not assigned to the company) are unaffected.
     */
    private void setCompanyAndUsersStatus(company existing, String status) {
        existing.setCompany_status(status);
        var users = userRepo.findByCompanyCompanyId(existing.getCompany_id());
        for (sys_user user : users) {
            user.setUser_status(status);
        }
        userRepo.saveAll(users);
    }
}