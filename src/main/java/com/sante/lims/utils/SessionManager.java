package com.sante.lims.utils;

import com.sante.lims.models.User;

public final class SessionManager {
    private static User currentUser;

    private SessionManager() {
    }

    public static void startSession(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void endSession() {
        currentUser = null;
    }
}
