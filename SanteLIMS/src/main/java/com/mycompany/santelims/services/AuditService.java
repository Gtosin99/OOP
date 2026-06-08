package com.mycompany.santelims.services;

import com.mycompany.santelims.dao.AuditLogDao;

public class AuditService {
    private static final AuditLogDao dao = new AuditLogDao();

    public static void log(String userEmail, String action, String details) {
        dao.log(userEmail, action, details);
    }

    public static void logLogin(String email) {
        log(email, "LOGIN", email + " logged in");
    }

    public static void logLogout(String email) {
        log(email, "LOGOUT", email + " logged out");
    }

    public static void logUserCreated(String byEmail, String newUserEmail, String role) {
        log(byEmail, "USER_CREATED", "Created user " + newUserEmail + " with role " + role);
    }

    public static void logUserDeleted(String byEmail, String deletedEmail) {
        log(byEmail, "USER_DELETED", "Deleted user " + deletedEmail);
    }

    public static void logPasswordChange(String email) {
        log(email, "PASSWORD_CHANGE", email + " changed their password");
    }

    public static void logTestCreated(String byEmail, String testName) {
        log(byEmail, "TEST_CREATED", "Created test type: " + testName);
    }

    public static void logTestUpdated(String byEmail, String testName) {
        log(byEmail, "TEST_UPDATED", "Updated test type: " + testName);
    }

    public static void logTestDeleted(String byEmail, String testName) {
        log(byEmail, "TEST_DELETED", "Deleted test type: " + testName);
    }
}