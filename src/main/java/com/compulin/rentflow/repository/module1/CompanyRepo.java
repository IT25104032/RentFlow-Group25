package com.compulin.rentflow.repository.module1;

import com.compulin.rentflow.entity.module1.company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRepo extends JpaRepository<company, Integer> {

    List<company> findByCompanyStatus(String companyStatus);

    List<company> findByCompanyNameContainingIgnoreCase(String companyName);

}