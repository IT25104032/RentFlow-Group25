package com.compulin.rentflow.repository.module1;

import com.compulin.rentflow.entity.module1.sys_user;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface sys_user_repo extends JpaRepository<sys_user, Integer> {

    Optional<sys_user> findByEmail(String email);

    @Query("""
        select u from sys_user u
        where u.userRole = :userRole
    """)
    List<sys_user> findByUserRole(
            @Param("userRole") String userRole
    );

    @Query("""
        select u from sys_user u
        where u.userStatus = :userStatus
    """)
    List<sys_user> findByUserStatus(
            @Param("userStatus") String userStatus
    );

    @Query("""
        select u from sys_user u
        where u.company.companyId = :companyId
    """)
    List<sys_user> findByCompanyId(
            @Param("companyId") Integer companyId
    );

    @Query("""
        select u from sys_user u
        where u.userId = :userId
        and u.company.companyId = :companyId
    """)
    Optional<sys_user> findByUserIdAndCompanyId(
            @Param("userId") Integer userId,
            @Param("companyId") Integer companyId
    );
}