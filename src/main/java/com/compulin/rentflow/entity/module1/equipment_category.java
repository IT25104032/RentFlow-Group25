package com.compulin.rentflow.entity.module1;

import jakarta.persistence.*;

@Entity
@Table(name = "equipment_category")
public class equipment_category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer category_id;


    /*
     * Foreign key:
     * equipment_category.company_id
     * -> company.company_id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private company company;


    @Column(
            name = "category_name",
            nullable = false,
            length = 100
    )
    private String category_name;


    @Column(
            name = "cat_description",
            length = 255
    )
    private String cat_description;


    @Column(
            name = "cat_status",
            nullable = false,
            length = 20
    )
    private String cat_status;


    public equipment_category() {
    }


    public Integer getCategory_id() {
        return category_id;
    }

    public void setCategory_id(Integer category_id) {
        this.category_id = category_id;
    }


    public company getCompany() {
        return company;
    }

    public void setCompany(company company) {
        this.company = company;
    }


    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }


    public String getCat_description() {
        return cat_description;
    }

    public void setCat_description(
            String cat_description) {

        this.cat_description = cat_description;
    }


    public String getCat_status() {
        return cat_status;
    }

    public void setCat_status(String cat_status) {
        this.cat_status = cat_status;
    }
}