package com.compulin.rentflow.service.module1;

import com.compulin.rentflow.entity.module1.sys_user;
import com.compulin.rentflow.repository.module1.sys_user_repo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class sys_user_service {

    private final sys_user_repo userRepo;

    public sys_user_service(sys_user_repo userRepo) {
        this.userRepo = userRepo;
    }

    // Get all users
    public List<sys_user> getAllUsers() {
        return userRepo.findAll();
    }

    // Get user by ID
    public sys_user getUserById(Integer id) {
        return userRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with ID: " + id));
    }

    // Get user by email
    public sys_user getUserByEmail(String email) {

        return userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with email: " + email));
    }

    // Get users by role
    public List<sys_user> getUsersByRole(String role) {
        return userRepo.findByUserRole(role);
    }

    // Get users by status
    public List<sys_user> getUsersByStatus(String status) {
        return userRepo.findByUserStatus(status);
    }

    // Get users by company
    public List<sys_user> getUsersByCompany(
            Integer companyId) {

        return userRepo.findByCompanyId(companyId);
    }

    // Create user
    public sys_user createUser(sys_user newUser) {

        if (newUser.getCreated_at() == null) {
            newUser.setCreated_at(LocalDateTime.now());
        }

        if (newUser.getUser_status() == null ||
                newUser.getUser_status().isBlank()) {

            newUser.setUser_status("ACTIVE");
        }

        return userRepo.save(newUser);
    }

    // Update user
    public sys_user updateUser(
            Integer id,
            sys_user userDetails) {

        sys_user existingUser = getUserById(id);

        if (userDetails.getCompany() != null) {
            existingUser.setCompany(
                    userDetails.getCompany());
        }

        if (userDetails.getFull_name() != null) {
            existingUser.setFull_name(
                    userDetails.getFull_name());
        }

        if (userDetails.getEmail() != null) {
            existingUser.setEmail(
                    userDetails.getEmail());
        }

        if (userDetails.getPassword_hash() != null) {
            existingUser.setPassword_hash(
                    userDetails.getPassword_hash());
        }

        if (userDetails.getPhone() != null) {
            existingUser.setPhone(
                    userDetails.getPhone());
        }

        if (userDetails.getUser_role() != null) {
            existingUser.setUser_role(
                    userDetails.getUser_role());
        }

        if (userDetails.getUser_status() != null) {
            existingUser.setUser_status(
                    userDetails.getUser_status());
        }

        return userRepo.save(existingUser);
    }

    // Change user status
    public sys_user updateUserStatus(
            Integer id,
            String status) {

        sys_user existingUser = getUserById(id);

        existingUser.setUser_status(status);

        return userRepo.save(existingUser);
    }

    // Change user role
    public sys_user updateUserRole(
            Integer id,
            String role) {

        sys_user existingUser = getUserById(id);

        existingUser.setUser_role(role);

        return userRepo.save(existingUser);
    }

    // Delete user
    public void deleteUser(Integer id) {

        sys_user existingUser = getUserById(id);

        userRepo.delete(existingUser);
    }
}