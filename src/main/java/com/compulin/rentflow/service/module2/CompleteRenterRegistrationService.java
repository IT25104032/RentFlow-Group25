package com.compulin.rentflow.service.module2;

import com.compulin.rentflow.dto.module2.CustomerRequest;
import com.compulin.rentflow.dto.module2.CustomerResponse;
import com.compulin.rentflow.dto.module2.CustomerSecondaryContactRequest;
import com.compulin.rentflow.entity.module2.Customer;
import com.compulin.rentflow.entity.module2.CustomerDocument;
import com.compulin.rentflow.entity.module2.CustomerSecondaryContact;
import com.compulin.rentflow.repository.module2.CustomerRepository;
import com.compulin.rentflow.repository.module2.CustomerDocumentRepository;
import com.compulin.rentflow.repository.module2.CustomerSecondaryContactRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Set;

@Service
public class CompleteRenterRegistrationService {
    private final CustomerRepository customers;
    private final CustomerDocumentRepository documents;
    private final CustomerSecondaryContactRepository contacts;
    private final CustomerDocumentStorageService storage;
    private final CustomerService customerService;
    private final Validator validator;

    public CompleteRenterRegistrationService(CustomerRepository customers,
                                             CustomerDocumentRepository documents,
                                             CustomerSecondaryContactRepository contacts,
                                             CustomerDocumentStorageService storage,
                                             CustomerService customerService,
                                             Validator validator) {
        this.customers = customers;
        this.documents = documents;
        this.contacts = contacts;
        this.storage = storage;
        this.customerService = customerService;
        this.validator = validator;
    }

    @Transactional(rollbackFor = Exception.class)
    public CustomerResponse register(CustomerRequest request,
                                     CustomerSecondaryContactRequest contact,
                                     String documentType, String documentNumber,
                                     MultipartFile documentFile, LocalDate expiryDate,
                                     String documentNotes, Integer authenticatedUserId) {
        validate(request, contact, documentType, documentNumber, documentFile);

        Customer customer = new Customer();
        customer.setCompanyId(request.getCompanyId());
        customer.setCustomerName(request.getCustomerName().trim());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setCustomerType(request.getCustomerType());
        customer.setCustomerStatus(request.getCustomerStatus());
        customer.setCreatedBy(authenticatedUserId);
        Customer savedCustomer = customers.saveAndFlush(customer);

        // The filesystem is not part of the DB transaction. Remove its file
        // automatically if the SQL transaction fails, including commit failures.
        String savedPath = storage.saveDocument(documentFile);
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            deleteFileQuietly(savedPath);
            throw new IllegalStateException("No active transaction for renter registration");
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != TransactionSynchronization.STATUS_COMMITTED) {
                    deleteFileQuietly(savedPath);
                }
            }
        });

        CustomerDocument document = new CustomerDocument();
        document.setCustomerId(savedCustomer.getCustomerId());
        document.setDocumentType(documentType.trim());
        document.setDocumentNumber(documentNumber.trim());
        document.setDocumentCopyPath(savedPath);
        document.setExpiryDate(expiryDate);
        document.setCheckedBy(authenticatedUserId);
        document.setNotes(documentNotes);
        documents.saveAndFlush(document);

        CustomerSecondaryContact secondary = new CustomerSecondaryContact();
        secondary.setCustomerId(savedCustomer.getCustomerId());
        secondary.setContactName(contact.getContactName().trim());
        secondary.setRelationship(contact.getRelationship());
        secondary.setPhoneNumber(contact.getPhoneNumber());
        secondary.setAlternatePhone(contact.getAlternatePhone());
        secondary.setEmail(contact.getEmail());
        secondary.setAddress(contact.getAddress());
        secondary.setNotes(contact.getNotes());
        contacts.saveAndFlush(secondary);

        return customerService.getCustomerById(savedCustomer.getCustomerId(), savedCustomer.getCompanyId());
    }

    private void validate(CustomerRequest customer, CustomerSecondaryContactRequest contact,
                          String type, String number, MultipartFile file) {
        checkViolations(validator.validate(customer));
        checkViolations(validator.validate(contact));
        if (blank(customer.getCustomerName()) || blank(customer.getCustomerType()) || blank(customer.getCustomerStatus())
                || blank(contact.getContactName()) || blank(type) || blank(number) || file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Renter, document copy and secondary contact are required.");
        }
    }

    private void checkViolations(Set<? extends ConstraintViolation<?>> violations) {
        if (!violations.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    violations.iterator().next().getMessage());
        }
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }

    private void deleteFileQuietly(String storedPath) {
        try {
            // Only delete a path produced by our own storage service.
            Path allowed = Path.of("uploads/customer-documents").toAbsolutePath().normalize();
            Path path = Path.of(storedPath).toAbsolutePath().normalize();
            if (path.startsWith(allowed)) Files.deleteIfExists(path);
        } catch (Exception ignored) {
            // Does not conceal the original transaction failure.
        }
    }
}

