package com.compulin.rentflow.controller.module4;

import com.compulin.rentflow.security.Module1SessionAccess;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Uses only Module 1's authenticated identity; no demonstration fallbacks. */
final class Module4Session {
    private Module4Session() {}

    static Integer userId(HttpSession session, Integer ignoredClientUserId) {
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
        }
        Object value = session.getAttribute("RENTFLOW_USER_ID");
        if (value instanceof Number number) {
            return number.intValue();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Valid user session required");
    }

    static Integer companyId(HttpSession session) {
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
        }
        Integer company = Module1SessionAccess.getCompanyId(session);
        if (company == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Company access required");
        }
        return company;
    }
}
