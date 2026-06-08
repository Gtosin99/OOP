package com.sante.lims.util;

public final class SessionContext {
    private static long currentCustomerId = 1L;
    private static long currentUserId = 1L;
    private static String currentUserEmail = "";
    private static String currentUserRole = "CUSTOMER";

    private SessionContext() {
    }

    public static long getCurrentCustomerId() {
        return currentCustomerId;
    }

    public static void setCurrentCustomerId(long customerId) {
        currentCustomerId = customerId;
    }

    public static long getCurrentUserId() {
        return currentUserId;
    }

    public static String getCurrentUserEmail() {
        return currentUserEmail;
    }

    public static String getCurrentUserRole() {
        return currentUserRole;
    }

    public static void setCurrentUser(long userId, String email, String role) {
        currentUserId = userId;
        currentUserEmail = email;
        currentUserRole = role;
        if ("CUSTOMER".equalsIgnoreCase(role)) {
            currentCustomerId = userId;
        }
    }

    public static void clear() {
        currentUserId = 0L;
        currentCustomerId = 0L;
        currentUserEmail = "";
        currentUserRole = "";
    }
}
