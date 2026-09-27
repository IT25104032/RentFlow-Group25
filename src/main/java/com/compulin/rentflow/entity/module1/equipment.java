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
    private Integer equipment_id;


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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private equipment_category category;


    @Column(
            name = "item_name",
            nullable = false,
            length = 150
    )
    private String item_name;


    @Column(
            name = "item_code",
            length = 80
    )
    private String item_code;


    @Column(
            name = "equ_description",
            length = 500
    )
    private String equ_description;


    @Column(
            name = "rental_rate",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal rental_rate;


    @Column(
            name = "rate_period",
            nullable = false,
            length = 20
    )
    private String rate_period;


    @Column(
            name = "refundable_deposit_per_unit",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal refundable_deposit_per_unit;


    @Column(
            name = "total_quantity",
            nullable = false
    )
    private Integer total_quantity;


    @Column(
            name = "available_quantity",
            nullable = false
    )
    private Integer available_quantity;


    @Column(
            name = "equ_status",
            nullable = false,
            length = 30
    )
    private String equ_status;


    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime created_at;


    public equipment() {
    }


    public Integer getEquipment_id() {
        return equipment_id;
    }

    public void setEquipment_id(Integer equipment_id) {
        this.equipment_id = equipment_id;
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
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }


    public String getItem_code() {
        return item_code;
    }

    public void setItem_code(String item_code) {
        this.item_code = item_code;
    }


    public String getEqu_description() {
        return equ_description;
    }

    public void setEqu_description(
            String equ_description) {

        this.equ_description = equ_description;
    }


    public BigDecimal getRental_rate() {
        return rental_rate;
    }

    public void setRental_rate(
            BigDecimal rental_rate) {

        this.rental_rate = rental_rate;
    }


    public String getRate_period() {
        return rate_period;
    }

    public void setRate_period(
            String rate_period) {

        this.rate_period = rate_period;
    }


    public BigDecimal
    getRefundable_deposit_per_unit() {

        return refundable_deposit_per_unit;
    }

    public void setRefundable_deposit_per_unit(
            BigDecimal refundable_deposit_per_unit) {

        this.refundable_deposit_per_unit =
                refundable_deposit_per_unit;
    }


    public Integer getTotal_quantity() {
        return total_quantity;
    }

    public void setTotal_quantity(
            Integer total_quantity) {

        this.total_quantity =
                total_quantity;
    }


    public Integer getAvailable_quantity() {
        return available_quantity;
    }

    public void setAvailable_quantity(
            Integer available_quantity) {

        this.available_quantity =
                available_quantity;
    }


    public String getEqu_status() {
        return equ_status;
    }

    public void setEqu_status(
            String equ_status) {

        this.equ_status = equ_status;
    }


    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(
            LocalDateTime created_at) {

        this.created_at = created_at;
    }

}