package com.compulin.rentflow.entity.module1;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipment")
public class equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equipment_id")
    private Integer equipmentId;


    /*
     * Foreign key:
     * equipment.company_id -> company.company_id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private company company;


    /*
     * Foreign key:
     * equipment.category_id
     * -> equipment_category.category_id
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private equipment_category category;


    @Column(
            name = "item_name",
            nullable = false,
            length = 150
    )
    private String itemName;


    @Column(
            name = "item_code",
            length = 80
    )
    private String itemCode;


    @Column(
            name = "equ_description",
            length = 500
    )
    private String equDescription;


    @Column(
            name = "rental_rate",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal rentalRate;


    @Column(
            name = "rate_period",
            nullable = false,
            length = 20
    )
    private String ratePeriod;


    @Column(
            name = "security_deposit_per_unit",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal securityDepositPerUnit;


    @Column(
            name = "total_quantity",
            nullable = false
    )
    private Integer totalQuantity;


    @Column(
            name = "available_quantity",
            nullable = false
    )
    private Integer availableQuantity;


    @Column(
            name = "equ_status",
            nullable = false,
            length = 30
    )
    private String equStatus;


    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;


    public equipment() {
    }


    public Integer getEquipment_id() {
        return equipmentId;
    }

    public void setEquipment_id(Integer equipment_id) {
        this.equipmentId = equipment_id;
    }


    public company getCompany() {
        return company;
    }

    public void setCompany(company company) {
        this.company = company;
    }


    public equipment_category getCategory() {
        return category;
    }

    public void setCategory(
            equipment_category category) {

        this.category = category;
    }


    public String getItem_name() {
        return itemName;
    }

    public void setItem_name(String item_name) {
        this.itemName = item_name;
    }


    public String getItem_code() {
        return itemCode;
    }

    public void setItem_code(String item_code) {
        this.itemCode = item_code;
    }


    public String getEqu_description() {
        return equDescription;
    }

    public void setEqu_description(
            String equDescription) {

        this.equDescription = equDescription;
    }


    public BigDecimal getRental_rate() {
        return rentalRate;
    }

    public void setRental_rate(
            BigDecimal rentalRate) {

        this.rentalRate = rentalRate;
    }


    public String getRate_period() {
        return ratePeriod;
    }

    public void setRate_period(
            String rate_period) {

        this.ratePeriod = rate_period;
    }


    public BigDecimal
    getSecurity_deposit_per_unit() {

        return securityDepositPerUnit;
    }

    public void setSecurity_deposit_per_unit(
            BigDecimal security_deposit_per_unit) {

        this.securityDepositPerUnit =
                security_deposit_per_unit;
    }


    public Integer getTotal_quantity() {
        return totalQuantity;
    }

    public void setTotal_quantity(
            Integer total_quantity) {

        this.totalQuantity =
                total_quantity;
    }


    public Integer getAvailable_quantity() {
        return availableQuantity;
    }

    public void setAvailable_quantity(
            Integer available_quantity) {

        this.availableQuantity =
                available_quantity;
    }


    public String getEqu_status() {
        return equStatus;
    }

    public void setEqu_status(
            String equ_status) {

        this.equStatus = equ_status;
    }


    public LocalDateTime getCreated_at() {
        return createdAt;
    }

    public void setCreated_at(
            LocalDateTime created_at) {

        this.createdAt = created_at;
    }
}