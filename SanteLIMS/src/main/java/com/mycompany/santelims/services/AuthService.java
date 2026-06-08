package com.mycompany.santelims.services;

import com.mycompany.santelims.dao.UserDao;
import com.mycompany.santelims.models.User;
import com.mycompany.santelims.utils.Session;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {
    private final UserDao userDao = new UserDao();

    public String login(String email, String password) {
        if (email == null || email.trim().isEmpty()) return "Email is required";
        if (password == null || password.trim().isEmpty()) return "Password is required";

        User user = userDao.findByEmail(email.trim());
        if (user == null) return "User not found";
        if (user.getPasswordHash() == null) return "Account not configured properly";

        // ✅ BCrypt with plain text fallback
        boolean isValid;
        try {
            isValid = BCrypt.checkpw(password.trim(), user.getPasswordHash());
        } catch (Exception e) {
            isValid = password.trim().equals(user.getPasswordHash());
        }

        if (!isValid) return "Incorrect password";

        AuditService.logLogin(email.trim());
        Session.setCurrentUser(user);

        if (user.isMustChangePassword()) {
            return "MUST_CHANGE_PASSWORD";
        }

        return "SUCCESS";
    }
}