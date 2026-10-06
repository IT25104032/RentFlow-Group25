package com.compulin.rentflow.entity.module1;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_user")
public class sys_user {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;


    /*
     * Foreign key:
     * sys_user.company_id -> company.company_id
     *
     * NULL is allowed because the Compulin Admin
     * does not belong to a rental company.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private company company;


    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;


    @Column(name = "email", nullable = false, unique = true, length = 120)
    private String email;


    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;


    @Column(name = "phone", length = 25)
    private String phone;


    @Column(name = "user_role", nullable = false, length = 30)
    private String userRole;


    @Column(name = "user_status", nullable = false, length = 20)
    private String userStatus;


    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    public sys_user() {
    }


    public Integer getUser_id() {
        return userId;
    }

    public void setUser_id(Integer user_id) {
        this.userId = user_id;
    }


    public company getCompany() {
        return company;
    }

    public void setCompany(company company) {
        this.company = company;
    }


    public String getFull_name() {
        return fullName;
    }

    public void setFull_name(String full_name) {
        this.fullName = full_name;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getPassword_hash() {
        return passwordHash;
    }

    public void setPassword_hash(String password_hash) {
        this.passwordHash = password_hash;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    public String getUser_role() {
        return userRole;
    }

    public void setUser_role(String user_role) {
        this.userRole = user_role;
    }


    public String getUser_status() {
        return userStatus;
    }

    public void setUser_status(String user_status) {
        this.userStatus = user_status;
    }


    public LocalDateTime getCreated_at() {
        return createdAt;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.createdAt = created_at;
    }
}