package com.compulin.rentflow.service.module2;


import com.compulin.rentflow.dto.module2.CustomerSecondaryContactRequest;
import com.compulin.rentflow.dto.module2.CustomerSecondaryContactResponse;
import com.compulin.rentflow.entity.module2.CustomerSecondaryContact;
import com.compulin.rentflow.repository.module2.CustomerSecondaryContactRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerSecondaryContactService {

    private final CustomerSecondaryContactRepository repository;

    public CustomerSecondaryContactService(
            CustomerSecondaryContactRepository repository) {

        this.repository = repository;
    }

    public CustomerSecondaryContactResponse createContact(
            CustomerSecondaryContactRequest request) {

        Optional<CustomerSecondaryContact> existingContact =
                repository.findByCustomerId(request.getCustomerId());

        if (existingContact.isPresent()) {
            return null;
        }

        CustomerSecondaryContact contact =
                new CustomerSecondaryContact();

        contact.setCustomerId(request.getCustomerId());
        contact.setContactName(request.getContactName());
        contact.setRelationship(request.getRelationship());
        contact.setPhoneNumber(request.getPhoneNumber());
        contact.setAlternatePhone(request.getAlternatePhone());
        contact.setEmail(request.getEmail());
        contact.setAddress(request.getAddress());
        contact.setNotes(request.getNotes());

        CustomerSecondaryContact savedContact =
                repository.save(contact);

        return convertToResponse(savedContact);
    }

    public CustomerSecondaryContactResponse getContact(
            Integer customerId) {

        Optional<CustomerSecondaryContact> optionalContact =
                repository.findByCustomerId(customerId);

        if (optionalContact.isEmpty()) {
            return null;
        }

        return convertToResponse(optionalContact.get());
    }

    public CustomerSecondaryContactResponse updateContact(
            Integer secondaryContactId,
            Integer customerId,
            CustomerSecondaryContactRequest request) {

        Optional<CustomerSecondaryContact> optionalContact =
                repository.findBySecondaryContactIdAndCustomerId(
                        secondaryContactId,
                        customerId
                );

        if (optionalContact.isEmpty()) {
            return null;
        }

        CustomerSecondaryContact contact =
                optionalContact.get();

        contact.setContactName(request.getContactName());
        contact.setRelationship(request.getRelationship());
        contact.setPhoneNumber(request.getPhoneNumber());
        contact.setAlternatePhone(request.getAlternatePhone());
        contact.setEmail(request.getEmail());
        contact.setAddress(request.getAddress());
        contact.setNotes(request.getNotes());

        CustomerSecondaryContact updatedContact =
                repository.save(contact);

        return convertToResponse(updatedContact);
    }

    private CustomerSecondaryContactResponse convertToResponse(
            CustomerSecondaryContact contact) {

        CustomerSecondaryContactResponse response =
                new CustomerSecondaryContactResponse();

        response.setSecondaryContactId(
                contact.getSecondaryContactId()
        );

        response.setCustomerId(
                contact.getCustomerId()
        );

        response.setContactName(
                contact.getContactName()
        );

        response.setRelationship(
                contact.getRelationship()
        );

        response.setPhoneNumber(
                contact.getPhoneNumber()
        );

        response.setAlternatePhone(
                contact.getAlternatePhone()
        );

        response.setEmail(
                contact.getEmail()
        );

        response.setAddress(
                contact.getAddress()
        );

        response.setNotes(
                contact.getNotes()
        );

        return response;
    }
}