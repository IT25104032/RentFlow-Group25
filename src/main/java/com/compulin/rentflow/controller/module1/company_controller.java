package com.compulin.rentflow.controller.module1;

import com.compulin.rentflow.dto.module1.CompanyRegistrationRequest;
import com.compulin.rentflow.entity.module1.company;
import com.compulin.rentflow.repository.module1.CompanyRepo;
import com.compulin.rentflow.service.module1.company_service;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/module1/companies")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
public class company_controller {

    private final CompanyRepo companyRepo;
    private final company_service companyService;

    public company_controller(
            CompanyRepo companyRepo,
            company_service companyService
    ) {
        this.companyRepo = companyRepo;
        this.companyService = companyService;
    }

    @GetMapping
    public ResponseEntity<?> getAllCompanies(
            HttpSession session
    ) {

        String role =
                (String) session.getAttribute(
                        "RENTFLOW_ROLE"
                );

        if (!"COMPULIN_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(Map.of(
                            "message",
                            "Access denied"
                    ));
        }

        return ResponseEntity.ok(
                companyRepo.findAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCompanyById(
            @PathVariable Integer id,
            HttpSession session
    ) {

        String role =
                (String) session.getAttribute(
                        "RENTFLOW_ROLE"
                );

        if (!"COMPULIN_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(Map.of(
                            "message",
                            "Access denied"
                    ));
        }

        try {
            return ResponseEntity.ok(
                    companyService.getCompanyById(id)
            );
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> registerCompany(
            @RequestBody CompanyRegistrationRequest request,
            HttpSession session
    ) {

        String role =
                (String) session.getAttribute(
                        "RENTFLOW_ROLE"
                );

        Integer userId =
                (Integer) session.getAttribute(
                        "RENTFLOW_USER_ID"
                );

        if (!"COMPULIN_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(Map.of(
                            "message",
                            "Only Compulin Administrator can register companies"
                    ));
        }

        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(Map.of(
                            "message",
                            "Please log in"
                    ));
        }

        try {

            company saved =
                    companyService.registerCompany(
                            request,
                            userId
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Company registered successfully",
                            "companyId",
                            saved.getCompany_id(),
                            "companyName",
                            saved.getCompany_name(),
                            "username",
                            saved.getEmail(),
                            "role",
                            "COMPANY_ADMIN"
                    )
            );

        } catch (RuntimeException ex) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            ex.getMessage()
                    ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCompany(
            @PathVariable Integer id,
            @RequestBody company companyDetails,
            HttpSession session
    ) {

        String role =
                (String) session.getAttribute(
                        "RENTFLOW_ROLE"
                );

        if (!"COMPULIN_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(Map.of(
                            "message",
                            "Access denied"
                    ));
        }

        try {

            company updated =
                    companyService.updateCompany(
                            id,
                            companyDetails
                    );

            return ResponseEntity.ok(updated);

        } catch (RuntimeException ex) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            ex.getMessage()
                    ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deactivateCompany(
            @PathVariable Integer id,
            HttpSession session
    ) {

        String role =
                (String) session.getAttribute(
                        "RENTFLOW_ROLE"
                );

        if (!"COMPULIN_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(Map.of(
                            "message",
                            "Access denied"
                    ));
        }

        try {

            companyService.deactivateCompany(id);

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Company deactivated successfully"
                    )
            );

        } catch (RuntimeException ex) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            ex.getMessage()
                    ));
        }
    }
}