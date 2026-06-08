package com.sante.lims.service;

import com.sante.lims.dao.CustomerModuleRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class RegistrationService {
    private final CustomerModuleRepository repository = new CustomerModuleRepository();
    private final EmailService emailService = new EmailService();

    public long selfRegister(String fullName, String email, String plainPassword) throws Exception {
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        String token = UUID.randomUUID().toString();

        long userId = repository.createCustomerSelfRegistration(fullName, email, hashedPassword, token);
        emailService.sendRegistrationVerificationRequired(email, token);
        repository.createNotification(userId, "Verification Required", "Please verify your email using the token sent to your inbox.");

        return userId;
    }

    public boolean verifyEmailToken(String token, String email) throws Exception {
        boolean verified = repository.verifyEmailToken(token);
        if (verified) {
            emailService.sendEmailVerified(email);
        }
        return verified;
    }
}
