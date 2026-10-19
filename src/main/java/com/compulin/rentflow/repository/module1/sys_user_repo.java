//sys_user
package com.compulin.rentflow.repository.module1;

import com.compulin.rentflow.entity.module1.sys_user;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface sys_user_repo
        extends JpaRepository<sys_user, Integer> {

    @Query(value = """
        SELECT * FROM sys_user
        WHERE email = :email
        LIMIT 1
        """, nativeQuery = true)
    Optional<sys_user> findByEmail(
            @Param("email") String email
    );

    @Query(value = """
        SELECT * FROM sys_user
        WHERE user_role = :userRole
        """, nativeQuery = true)
    List<sys_user> findByUserRole(
            @Param("userRole") String userRole
    );

    @Query(value = """
        SELECT * FROM sys_user
        WHERE user_status = :userStatus
        """, nativeQuery = true)
    List<sys_user> findByUserStatus(
            @Param("userStatus") String userStatus
    );

    @Query(value = """
        SELECT * FROM sys_user
        WHERE company_id = :companyId
        ORDER BY full_name
        """, nativeQuery = true)
    List<sys_user> findByCompanyCompanyId(
            @Param("companyId") Integer companyId
    );

    // Load user's company in the same query for authentication checks.
    @Query("select u from sys_user u left join fetch u.company where u.userId = :userId")
    Optional<sys_user> findByIdWithCompany(@Param("userId") Integer userId);

    @Query(value = """
        SELECT * FROM sys_user
        WHERE user_id = :userId
          AND company_id = :companyId
        LIMIT 1
        """, nativeQuery = true)
    Optional<sys_user> findByUserIdAndCompanyId(
            @Param("userId") Integer userId,
            @Param("companyId") Integer companyId
    );
}
