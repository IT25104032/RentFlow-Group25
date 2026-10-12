package com.compulin.rentflow.dto.module2;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CustomerSecondaryContactRequest {

    private Integer customerId;
    private String contactName;
    private String relationship;

    @NotBlank(message = "Secondary contact phone number is required.")
    @Pattern(regexp = "[0-9]{10}", message = "Secondary contact phone number must contain exactly 10 digits.")
    private String phoneNumber;

    // Optional: blank is accepted; if supplied, it must contain exactly 10 digits.
    @Pattern(regexp = "^$|[0-9]{10}", message = "Alternate phone number must contain exactly 10 digits when provided.")
    private String alternatePhone;

    private String email;
    private String address;
    private String notes;

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAlternatePhone() { return alternatePhone; }
    public void setAlternatePhone(String alternatePhone) { this.alternatePhone = alternatePhone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
