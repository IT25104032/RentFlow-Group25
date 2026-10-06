package com.compulin.rentflow.entity.module1;

import jakarta.persistence.*;

@Entity
@Table(name = "equipment_category")
public class equipment_category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer categoryId;


    /*
     * Foreign key:
     * equipment_category.company_id
     * -> company.company_id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private company companyId;


    @Column(
            name = "category_name",
            nullable = false,
            length = 100
    )
    private String categoryName;


    @Column(
            name = "cat_description",
            length = 255
    )
    private String catDescription;


    @Column(
            name = "cat_status",
            nullable = false,
            length = 20
    )
    private String catStatus;


    public equipment_category() {
    }


    public Integer getCategory_id() {
        return categoryId;
    }

    public void setCategory_id(Integer category_id) {
        this.categoryId = category_id;
    }


    public company getCompany() {
        return companyId;
    }

    public void setCompany(company companyId) {
        this.companyId = companyId;
    }


    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }


    public String getCatDescription() {
        return catDescription;
    }

    public void setCatDescription(
            String catDescription) {

        this.catDescription = catDescription;
    }


    public String getCat_status() {
        return catStatus;
    }

    public void setCat_status(String cat_status) {
        this.catStatus = cat_status;
    }



}