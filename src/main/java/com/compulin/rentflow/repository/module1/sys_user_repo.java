package com.compulin.rentflow.repository.module1;

import com.compulin.rentflow.entity.module1.sys_user;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface sys_user_repo extends JpaRepository<sys_user, Integer> {

    Optional<sys_user> findByEmail(String email);

    List<sys_user> findByUserRole(String userRole);

    List<sys_user> findByUserStatus(String userStatus);

    List<sys_user> findByCompanyCompanyId(Integer companyId);
}
