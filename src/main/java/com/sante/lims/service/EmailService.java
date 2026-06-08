package com.sante.lims.service;

import com.sante.lims.util.AppConfig;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailService {

    private Session createSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", AppConfig.get("smtp.host"));
        props.put("mail.smtp.port", AppConfig.get("smtp.port"));
        props.put("mail.smtp.auth", AppConfig.get("smtp.auth"));
        props.put("mail.smtp.starttls.enable", AppConfig.get("smtp.starttls"));

        final String username = AppConfig.get("smtp.username");
        final String password = AppConfig.get("smtp.password");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    public void sendEmail(String to, String subject, String body) throws MessagingException {
        if (!isConfigured()) {
            throw new MessagingException("SMTP is not configured.");
        }

        Session session = createSession();
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(AppConfig.get("smtp.from")));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);
        Transport.send(message);
    }

    public boolean isConfigured() {
        String host = AppConfig.get("smtp.host");
        String username = AppConfig.get("smtp.username");
        String password = AppConfig.get("smtp.password");
        String from = AppConfig.get("smtp.from");

        return hasValue(host)
                && hasValue(username)
                && hasValue(password)
                && hasValue(from)
                && !username.equalsIgnoreCase("your-email@example.com")
                && !password.equalsIgnoreCase("your-app-password")
                && !from.equalsIgnoreCase("your-email@example.com");
    }

    private boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }

    public void sendRegistrationVerificationRequired(String to, String token) throws MessagingException {
        String subject = "Sante LIMS - Verify Your Email";
        String body = "Dear Customer,\n\n"
                + "Thank you for registering. Verification is required before full access.\n"
                + "Use this verification token in the app: " + token + "\n\n"
                + "Regards,\nSante Diagnostics Ltd";
        sendEmail(to, subject, body);
    }

    public void sendEmailVerified(String to) throws MessagingException {
        sendEmail(
                to,
                "Sante LIMS - Email Verified",
                "Your email has been successfully verified. You can now access all customer features."
        );
    }

    public void sendResultReady(String to, String testName) throws MessagingException {
        sendEmail(
                to,
                "Sante LIMS - Result Ready",
                "Your validated result for " + testName + " is now ready. Please log in to download/view it."
        );
    }

    public void sendImportantAccountNotification(String to, String message) throws MessagingException {
        sendEmail(to, "Sante LIMS - Account Notification", message);
    }
}
