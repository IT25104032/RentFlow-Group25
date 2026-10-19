package com.compulin.rentflow.security;

import com.compulin.rentflow.entity.module2.Rental;
import com.compulin.rentflow.entity.module3.Invoice;
import com.compulin.rentflow.entity.module3.SecurityDeposit;
import com.compulin.rentflow.repository.module2.RentalRepository;
import com.compulin.rentflow.repository.module3.InvoiceRepository;
import com.compulin.rentflow.repository.module3.SecurityDepositRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import java.util.Objects;

@Component
public class Module3Access {
    private final RentalRepository rentals;
    private final InvoiceRepository invoices;
    private final SecurityDepositRepository deposits;

    public Module3Access(RentalRepository rentals, InvoiceRepository invoices,
                         SecurityDepositRepository deposits) {
        this.rentals = rentals;
        this.invoices = invoices;
        this.deposits = deposits;
    }

    public Integer companyId(HttpSession session) {
        if (session == null || session.getAttribute("RENTFLOW_USER_ID") == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
        }
        Integer id = Module1SessionAccess.getCompanyId(session);
        if (id == null || !Module1SessionAccess.hasAnyRole(session, "COMPANY_ADMIN", "RENTAL_OFFICER")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Company access required");
        }
        return id;
    }

    public Integer userId(HttpSession session) {
        companyId(session);
        Object id = session.getAttribute("RENTFLOW_USER_ID");
        if (id instanceof Number n) return n.intValue();
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Valid staff session required");
    }

    public void rental(Integer rentalId, HttpSession session) {
        Integer companyId = companyId(session);
        Rental rental = rentals.findById(rentalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rental not found"));
        if (!Objects.equals(rental.getCompanyId(), companyId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Rental belongs to another company");
        }
    }

    public void invoice(Integer invoiceId, HttpSession session) {
        Invoice invoice = invoices.findById(invoiceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
        rental(invoice.getRentalId(), session);
    }

    public void deposit(Integer depositId, HttpSession session) {
        SecurityDeposit deposit = deposits.findById(depositId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Deposit not found"));
        rental(deposit.getRentalId(), session);
    }
}
