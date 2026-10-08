package com.compulin.rentflow.controller.module4;

import jakarta.servlet.http.HttpSession;

/*
 * MODULE 4 - who is working.
 * The shared login (Module 1) saves the user and company in the session
 * under these names. This branch has no login page yet, so when nobody is
 * logged in Module 4 works as the seed rental officer Nimal Perera
 * (user 3, company 1000) and shows every company's rentals.
 */
final class Module4Session {

    static final String SESSION_USER_ID = "RENTFLOW_USER_ID";
    static final String SESSION_COMPANY_ID = "RENTFLOW_COMPANY_ID";

    /** Seed RENTAL_OFFICER used until the shared login is merged in. */
    static final Integer DEMO_USER_ID = 3;

    private Module4Session() {
    }

    static Integer userId(HttpSession session, Integer fallback) {
        Object id = session.getAttribute(SESSION_USER_ID);
        if (id instanceof Integer i) {
            return i;
        }
        return fallback != null ? fallback : DEMO_USER_ID;
    }

    /** null = every company (Compulin admin, or no login on this branch). */
    static Integer companyId(HttpSession session) {
        Object id = session.getAttribute(SESSION_COMPANY_ID);
        return id instanceof Integer i ? i : null;
    }
}
