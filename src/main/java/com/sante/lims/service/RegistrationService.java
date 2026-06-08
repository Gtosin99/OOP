package com.sante.lims.service;

import com.sante.lims.dao.CustomerModuleRepository;
import jakarta.mail.MessagingException;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class RegistrationService {
    private static RegistrationResult lastRegistrationResult;

    private final CustomerModuleRepository repository = new CustomerModuleRepository();
    private final EmailService emailService = new EmailService();

    public RegistrationResult selfRegister(String fullName, String email, String plainPassword) throws Exception {
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        String token = UUID.randomUUID().toString();

        long userId = repository.createCustomerSelfRegistration(fullName, email, hashedPassword, token);
        boolean emailSent = true;
        String emailStatus = "Verification email sent.";
        try {
            emailService.sendRegistrationVerificationRequired(email, token);
        } catch (MessagingException ex) {
            emailSent = false;
            emailStatus = "Email was not sent because SMTP is not configured. Use the token shown in the app.";
        }

        String notificationMessage = emailSent
                ? "Please verify your email using the token sent to your inbox."
                : "SMTP is not configured. Use this verification token in the app: " + token;
        repository.createNotification(userId, "Verification Required", notificationMessage);

        lastRegistrationResult = new RegistrationResult(userId, email, token, emailSent, emailStatus);
        return lastRegistrationResult;
    }

    public boolean verifyEmailToken(String token, String email) throws Exception {
        boolean verified = repository.verifyEmailToken(token);
        if (verified) {
            try {
                emailService.sendEmailVerified(email);
            } catch (MessagingException ex) {
                // Verification should not fail just because optional email delivery is unavailable.
            }
        }
        return verified;
    }

    public static RegistrationResult getLastRegistrationResult() {
        return lastRegistrationResult;
    }

    public record RegistrationResult(long userId, String email, String token, boolean emailSent, String emailStatus) {
    }
}
