package com.compulin.rentflow.entity.module2;


import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_document")
public class CustomerDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Integer documentId;

    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    @Column(name = "document_type", nullable = false, length = 30)
    private String documentType;

    @Column(name = "document_number", nullable = false, length = 80)
    private String documentNumber;

    @Column(name = "document_copy_path", nullable = false, length = 500)
    private String documentCopyPath;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "checked_by", nullable = false)
    private Integer checkedBy;

    @Column(name = "checked_at", nullable = false)
    private LocalDateTime checkedAt;

    @Column(name = "notes", length = 255)
    private String notes;

    @PrePersist
    protected void onCreate() {
        checkedAt = LocalDateTime.now();
    }

    public Integer getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Integer documentId) {
        this.documentId = documentId;
    }

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

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(LocalDateTime checkedAt) {
        this.checkedAt = checkedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}