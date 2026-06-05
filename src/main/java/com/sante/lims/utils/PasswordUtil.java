package com.sante.lims.utils;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {
    private static final int WORKLOAD = 12;

    private PasswordUtil() {
    }

    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(WORKLOAD));
    }

    public static boolean verifyPassword(String plainPassword, String passwordHash) {
        if (plainPassword == null || passwordHash == null || passwordHash.isBlank()) {
            return false;
        }

        return BCrypt.checkpw(plainPassword, passwordHash);
    }
}
