package com.compulin.rentflow.service.module1;

import com.compulin.rentflow.entity.module1.company;
import com.compulin.rentflow.repository.module1.CompanyRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class company_service {

    private final CompanyRepo companyRepo;

    public company_service(CompanyRepo companyRepo) {
        this.companyRepo = companyRepo;
    }

    // Get all companies
    public List<company> getAllCompanies() {
        return companyRepo.findAll();
    }

    // Get company by ID
    public company getCompanyById(Integer id) {
        return companyRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Company not found with ID: " + id));
    }

    // Search companies by name
    public List<company> searchCompanies(String name) {
        return companyRepo.findByCompanyNameContainingIgnoreCase(name);
    }

    // Get companies by status
    public List<company> getCompaniesByStatus(String status) {
        return companyRepo.findByCompanyStatus(status);
    }

    // Create company
    public company createCompany(company newCompany) {

        if (newCompany.getRegistration_date() == null) {
            newCompany.setRegistration_date(LocalDateTime.now());
        }

        if (newCompany.getCompany_status() == null ||
                newCompany.getCompany_status().isBlank()) {

            newCompany.setCompany_status("ACTIVE");
        }

        return companyRepo.save(newCompany);
    }

    // Update company
    public company updateCompany(
            Integer id,
            company companyDetails) {

        company existingCompany = getCompanyById(id);

        if (companyDetails.getCompany_name() != null) {
            existingCompany.setCompany_name(
                    companyDetails.getCompany_name());
        }

        if (companyDetails.getRegistration_no() != null) {
            existingCompany.setRegistration_no(
                    companyDetails.getRegistration_no());
        }

        if (companyDetails.getEmail() != null) {
            existingCompany.setEmail(
                    companyDetails.getEmail());
        }

        if (companyDetails.getPhone() != null) {
            existingCompany.setPhone(
                    companyDetails.getPhone());
        }

        if (companyDetails.getAddress() != null) {
            existingCompany.setAddress(
                    companyDetails.getAddress());
        }

        if (companyDetails.getRegistered_by() != null) {
            existingCompany.setRegistered_by(
                    companyDetails.getRegistered_by());
        }

        if (companyDetails.getRegistration_date() != null) {
            existingCompany.setRegistration_date(
                    companyDetails.getRegistration_date());
        }

        if (companyDetails.getCompany_status() != null) {
            existingCompany.setCompany_status(
                    companyDetails.getCompany_status());
        }

        return companyRepo.save(existingCompany);
    }

    // Delete company
    public void deleteCompany(Integer id) {

        company existingCompany = getCompanyById(id);

        companyRepo.delete(existingCompany);
    }
}