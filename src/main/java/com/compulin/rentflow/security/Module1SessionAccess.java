
package com.compulin.rentflow.security;

import jakarta.servlet.http.HttpSession;

import java.util.Locale;
import java.util.Set;

public final class Module1SessionAccess {

    private Module1SessionAccess() {
    }

    public static Integer getCompanyId(HttpSession session) {
        Object value = session.getAttribute("RENTFLOW_COMPANY_ID");

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException exception) {
                return null;
            }
        }

        return null;
    }

    public static String getRole(HttpSession session) {
        Object value = session.getAttribute("RENTFLOW_ROLE");

        if (!(value instanceof String)) {
            return "";
        }

        String role = ((String) value)
                .trim()
                .toUpperCase(Locale.ROOT);

        if (role.startsWith("ROLE_")) {
            role = role.substring(5);
        }

        return role;
    }

    public static boolean hasAnyRole(
            HttpSession session,
            String... allowedRoles) {

        String currentRole = getRole(session);

        return Set.of(allowedRoles).contains(currentRole);
    }
}

