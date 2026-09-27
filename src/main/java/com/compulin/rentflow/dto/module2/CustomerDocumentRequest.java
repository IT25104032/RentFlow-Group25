package com.compulin.rentflow.dto.module2;


import java.time.LocalDate;

public class CustomerDocumentRequest {

    private Integer customerId;
    private String documentType;
    private String documentNumber;
    private String documentCopyPath;
    private LocalDate expiryDate;
    private Integer checkedBy;
    private String notes;

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getDocumentCopyPath() {
        return documentCopyPath;
    }

    public void setDocumentCopyPath(String documentCopyPath) {
        this.documentCopyPath = documentCopyPath;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Integer getCheckedBy() {
        return checkedBy;
    }

    public void setCheckedBy(Integer checkedBy) {
        this.checkedBy = checkedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}