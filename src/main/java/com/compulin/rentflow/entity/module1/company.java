package com.compulin.rentflow.entity.module1;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "company")
public class company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private Integer companyId;

    @Column(name = "company_name", nullable = false, length = 150)
    private String companyName;

    @Column(name = "registration_no", unique = true, length = 60)
    private String registrationNo;

    @Column(name = "email", nullable = false, length = 120)
    private String email;

    @Column(name = "phone", length = 25)
    private String phone;

    @Column(name = "address", length = 255)
    private String address;

    /*
     * Foreign key:
     * company.registered_by -> sys_user.user_id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_by", nullable = false)
    private sys_user registeredBy;

    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate;

    @Column(name = "company_status", nullable = false, length = 20)
    private String companyStatus;


    public company() {
    }


    public Integer getCompany_id() {
        return companyId;
    }

    public void setCompany_id(Integer company_id) {
        this.companyId = company_id;
    }


    public String getCompany_name() {
        return companyName;
    }

    public void setCompany_name(String company_name) {
        this.companyName = company_name;
    }


    public String getRegistration_no() {
        return registrationNo;
    }

    public void setRegistration_no(String registration_no) {
        this.registrationNo = registration_no;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    public sys_user getRegistered_by() {
        return registeredBy;
    }

    public void setRegistered_by(sys_user registered_by) {
        this.registeredBy = registered_by;
    }


    public LocalDateTime getRegistration_date() {
        return registrationDate;
    }

    public void setRegistration_date(
            LocalDateTime registration_date) {

        this.registrationDate = registration_date;
    }


    public String getCompany_status() {
        return companyStatus;
    }

    public void setCompany_status(String company_status) {
        this.companyStatus = company_status;
    }
}