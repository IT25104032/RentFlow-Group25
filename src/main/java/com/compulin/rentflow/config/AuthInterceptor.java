package com.compulin.rentflow.config;

import com.compulin.rentflow.controller.AuthController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        String path = request.getRequestURI();

        if (path.startsWith("/api/auth/")) {
            return true;
        }

        if (!path.startsWith("/api/module1/")) {
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

        return true;
    }
}
