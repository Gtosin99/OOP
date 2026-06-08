package com.mycompany.santelims.services;

import com.mycompany.santelims.dao.EmailVerificationDao;
import com.mycompany.santelims.dao.UserDao;
import com.mycompany.santelims.models.EmailVerification;
import java.time.LocalDateTime;
import java.util.UUID;

public class EmailVerificationService {

    private final EmailVerificationDao verificationDao = new EmailVerificationDao();
    private final UserDao userDao = new UserDao();

    public String generateAndSendToken(String userEmail) {
        // Generate a random token
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

        // Save to DB
        verificationDao.createToken(userEmail, token);

        // Simulate sending email — print to console
        System.out.println("========================================");
        System.out.println("=== VERIFICATION TOKEN FOR: " + userEmail);
        System.out.println("=== TOKEN: " + token);
        System.out.println("=== Expires in 24 hours");
        System.out.println("========================================");

        return token;
    }

    public String verifyToken(String token) {
        EmailVerification ev = verificationDao.findByToken(token);

        if (ev == null) {
            return "Invalid or already used token.";
        }

        if (ev.getExpiresAt().isBefore(LocalDateTime.now())) {
            return "Token has expired. Please register again.";
        }

        // Mark token as used
        verificationDao.markTokenUsed(token);

        // Mark user as verified
        userDao.markUserVerified(ev.getUserEmail());

        AuditService.log(ev.getUserEmail(), "EMAIL_VERIFIED",
            ev.getUserEmail() + " verified their email");

        return "SUCCESS";
    }
}