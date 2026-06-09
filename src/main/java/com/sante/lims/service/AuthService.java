package com.sante.lims.service;

import com.sante.lims.dao.UserDao;
import com.sante.lims.models.User;
import com.sante.lims.services.AuditLogService;
import com.sante.lims.util.SessionContext;
import com.sante.lims.utils.PasswordUtil;
import com.sante.lims.utils.SessionManager;

public class AuthService {
    private final UserDao userDao = new UserDao();
    private final AuditLogService auditLogService = new AuditLogService();

    public LoginResult login(String email, String password) {
        if (email == null || email.isBlank()) {
            return LoginResult.failure("Email is required.");
        }
        if (password == null || password.isBlank()) {
            return LoginResult.failure("Password is required.");
        }

        try {
            User user = userDao.findByEmail(email);
            if (user == null || !PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
                return LoginResult.failure("Invalid email or password.");
            }
            if (!user.isEmailVerified()) {
                return LoginResult.failure("Please verify your account before logging in.");
            }

            SessionManager.startSession(user);
            SessionContext.setCurrentUser(user.getId(), user.getEmail(), user.getRole());
            auditLogService.logAction(user.getId(), "LOGIN", "User logged in: " + user.getEmail());

            if (user.isForcePasswordChange()) {
                return LoginResult.mustChangePassword(user);
            }
            return LoginResult.success(user);
        } catch (Exception ex) {
            return LoginResult.failure("Unable to log in: " + ex.getMessage());
        }
    }

    public record LoginResult(boolean success, boolean mustChangePassword, String message, User user) {
        static LoginResult success(User user) {
            return new LoginResult(true, false, "SUCCESS", user);
        }

        static LoginResult mustChangePassword(User user) {
            return new LoginResult(false, true, "MUST_CHANGE_PASSWORD", user);
        }

        static LoginResult failure(String message) {
            return new LoginResult(false, false, message, null);
        }
    }
}
