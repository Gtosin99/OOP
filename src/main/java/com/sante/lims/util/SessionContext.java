package com.sante.lims.util;

public final class SessionContext {
    // Replace with authenticated user id after integrating full login module.
    private static long currentCustomerId = 1L;

    private SessionContext() {
    }

    public static long getCurrentCustomerId() {
        return currentCustomerId;
    }

    public static void setCurrentCustomerId(long customerId) {
        currentCustomerId = customerId;
    }
}
