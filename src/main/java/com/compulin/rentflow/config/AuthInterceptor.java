package com.compulin.rentflow.config;

import com.compulin.rentflow.controller.AuthController;
import com.compulin.rentflow.entity.module1.sys_user;
import com.compulin.rentflow.repository.module1.sys_user_repo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final sys_user_repo userRepo;

    public AuthInterceptor(sys_user_repo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        String path = request.getRequestURI();

        // Allow the frontend to obtain a CSRF token before login.
        if ("GET".equals(request.getMethod())
                && "/api/auth/csrf".equals(path)) {
            return true;
        }

        // Preflight requests contain no authenticated session.
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        // Permit unauthenticated login requests only for known login paths.
        // Verify the actual mapping in AuthController before deployment.
        if (HttpMethod.POST.matches(request.getMethod()) &&
                (path.equals("/api/auth/login") ||
                        path.equals("/api/module1/auth/login"))) {
            return true;
        }

        if (!path.startsWith("/api/")) {
            return true;
        }

        HttpSession session = request.getSession(false);

        if (session == null ||
                session.getAttribute(
                        AuthController.SESSION_USER_ID
                ) == null) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"message\":\"Please log in first.\"}"
            );

            return false;
        }

        String role = String.valueOf(
                session.getAttribute(
                        AuthController.SESSION_ROLE
                )
        );

        // Do not trust an old session after the company or user is deactivated.
        // Check against the database on each protected API request.
        Object rawUserId = session.getAttribute(AuthController.SESSION_USER_ID);
        Integer userId;
        try {
            userId = rawUserId instanceof Number number
                    ? number.intValue()
                    : Integer.valueOf(rawUserId.toString());
        } catch (NumberFormatException ex) {
            session.invalidate();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Invalid login session.\"}");
            return false;
        }

        sys_user currentUser = userRepo.findByIdWithCompany(userId).orElse(null);
        if (currentUser == null ||
                !"ACTIVE".equalsIgnoreCase(currentUser.getUser_status()) ||
                (currentUser.getCompany() != null &&
                        !"ACTIVE".equalsIgnoreCase(currentUser.getCompany().getCompany_status()))) {
            session.invalidate();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Account or company is inactive. Please contact your administrator.\"}");
            return false;
        }

        // Ensure that the session role matches the user's current database role.
        String databaseRole = currentUser.getUser_role();

        String sessionRole = role.startsWith("ROLE_")
                ? role.substring(5)
                : role;

        String normalizedDatabaseRole = databaseRole == null
                ? ""
                : databaseRole.startsWith("ROLE_")
                ? databaseRole.substring(5)
                : databaseRole;

        if (!normalizedDatabaseRole.equals(sessionRole)) {
            session.invalidate();

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"message\":\"Your account permissions have changed. Please log in again.\"}"
            );

            return false;
        }

        if (path.startsWith("/api/auth/")) {
            return true;
        }

        // Module 2/3/4 manage company data. Compulin administrators
        // must not acquire unrestricted access to renter records.
        if (!path.startsWith("/api/module1/")
                && !path.startsWith("/api/auth/")) {
            String normalizedRole = role.startsWith("ROLE_")
                    ? role.substring(5) : role;
            if (!"COMPANY_ADMIN".equals(normalizedRole)
                    && !"RENTAL_OFFICER".equals(normalizedRole)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"message\":\"Not authorized for company data.\"}");
                return false;
            }
            if (session.getAttribute(AuthController.SESSION_COMPANY_ID) == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"message\":\"A company session is required.\"}");
                return false;
            }
            return true;
        }

        if (path.startsWith("/api/module1/company-profile")) {
            return true; // The profile controller enforces COMPANY_ADMIN and session company.
        }

        if (path.startsWith("/api/module1/companies")) {

            if ("COMPULIN_ADMIN".equals(role)) {
                return true;
            }

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"message\":\"You are not authorized to manage companies.\"}"
            );

            return false;
        }

        if (path.startsWith("/api/module1/users")) {

            // Allow authenticated roles to view user information.
            if (HttpMethod.GET.matches(request.getMethod())) {
                if ("COMPANY_ADMIN".equals(role)
                        || "RENTAL_OFFICER".equals(role)
                        || "COMPULIN_ADMIN".equals(role)) {
                    return true;
                }
            }

            // Only Company Admin can create, edit or deactivate users.
            if ("COMPANY_ADMIN".equals(role)) {
                return true;
            }

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"message\":\"Only a company administrator can manage users.\"}"
            );

            return false;
        }

        if (path.startsWith(
                "/api/module1/equipment-categories")) {

            if ("COMPANY_ADMIN".equals(role) ||
                    "RENTAL_OFFICER".equals(role)) {

                return true;
            }

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"message\":\"You are not authorized to manage equipment categories.\"}"
            );

            return false;
        }

        if (path.startsWith("/api/module1/equipment")) {

            if ("COMPANY_ADMIN".equals(role) ||
                    "RENTAL_OFFICER".equals(role)) {

                return true;
            }

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"message\":\"You are not authorized to manage equipment.\"}"
            );

            return false;
        }

        // Do not permit unknown Module 1 routes by default.
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return false;
    }
}
