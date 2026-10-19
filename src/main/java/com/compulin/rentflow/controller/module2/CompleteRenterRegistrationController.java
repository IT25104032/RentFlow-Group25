package com.compulin.rentflow.controller.module2;

import com.compulin.rentflow.dto.module2.CustomerRequest;
import com.compulin.rentflow.dto.module2.CustomerResponse;
import com.compulin.rentflow.dto.module2.CustomerSecondaryContactRequest;
import com.compulin.rentflow.security.Module1SessionAccess;
import com.compulin.rentflow.service.module2.CompleteRenterRegistrationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
        import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.Objects;

@RestController
@RequestMapping("/api/customers")
public class CompleteRenterRegistrationController {
    private final CompleteRenterRegistrationService service;

    public CompleteRenterRegistrationController(CompleteRenterRegistrationService service) {
        this.service = service;
    }

    @PostMapping(value = "/complete-registration", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CustomerResponse> register(
            @Valid @RequestPart("customer") CustomerRequest customer,
            @Valid @RequestPart("contact") CustomerSecondaryContactRequest contact,
            @RequestParam String documentType,
            @RequestParam String documentNumber,
            @RequestParam("documentFile") MultipartFile documentFile,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate,
            @RequestParam(required = false) String notes,
            HttpSession session) {

        Integer companyId = Module1SessionAccess.getCompanyId(session);
        Object rawUserId = session == null ? null : session.getAttribute("RENTFLOW_USER_ID");
        if (companyId == null || !(rawUserId instanceof Number)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in first.");
        }
        if (!Module1SessionAccess.hasAnyRole(session, "COMPANY_ADMIN", "RENTAL_OFFICER")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to register renters.");
        }
        if (!Objects.equals(companyId, customer.getCompanyId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Company mismatch.");
        }
        // Never trust checkedBy, createdBy, or customerId supplied by the browser.
        customer.setCreatedBy(((Number) rawUserId).intValue());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                service.register(customer, contact, documentType, documentNumber,
                        documentFile, expiryDate, notes, ((Number) rawUserId).intValue())
        );
    }
}
