package com.example.gamesphere.service;

import com.example.gamesphere.config.EmailProperties;
import com.example.gamesphere.entity.Gift;
import com.example.gamesphere.entity.Order;
import com.example.gamesphere.entity.Payment;
import com.example.gamesphere.entity.Tournament;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final EmailProperties emailProperties;

    @Value("${spring.mail.username}")
    private String from;

    public void sendWelcomeEmail(User user) {
        send(
                user.getEmail(),
                "Welcome to Gamesphere",
                "Hi " + displayName(user) + ",\n\n"
                        + "Welcome to Gamesphere. Your account is ready to explore games, offers and tournaments.\n\n"
                        + "Gamesphere Team"
        );
    }

    public void sendPaymentSuccessEmail(User user, Order order, Payment payment) {
        send(
                user.getEmail(),
                "Payment successful - " + order.getOrderNumber(),
                "Hi " + displayName(user) + ",\n\n"
                        + "Your payment was completed successfully.\n"
                        + "Order number: " + order.getOrderNumber() + "\n"
                        + "Amount: " + payment.getAmount() + "\n"
                        + "Payment method: " + payment.getMethod() + "\n\n"
                        + "Gamesphere Team"
        );
    }

    public void sendGiftReceivedEmail(Gift gift) {
        String claimUrl = trimTrailingSlash(emailProperties.getFrontendUrl())
                + "/gifts/claim/" + gift.getClaimToken();

        send(
                gift.getRecipientEmail(),
                "You received a Gamesphere gift",
                "Hi,\n\n"
                        + gift.getSender().getUsername() + " sent you a gift: " + gift.getProduct().getName() + ".\n"
                        + "Claim link: " + claimUrl + "\n\n"
                        + "Gamesphere Team"
        );
    }

    public void sendTournamentReminderEmail(Tournament tournament, User user) {
        send(
                user.getEmail(),
                "Tournament reminder - " + tournament.getTitle(),
                "Hi " + displayName(user) + ",\n\n"
                        + "Reminder: " + tournament.getTitle() + " starts at " + tournament.getStartDate() + ".\n"
                        + "Game: " + tournament.getGame() + "\n\n"
                        + "Gamesphere Team"
        );
    }

    public void sendTestEmail(String to) {
        if (!emailProperties.isEnabled()) {
            throw new BusinessException("Email is disabled. Set MAIL_ENABLED=true first.");
        }
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            throw new BusinessException("JavaMailSender is not configured.");
        }

        try {
            mailSender.send(createMessage(
                    to,
                    "Gamesphere SMTP test",
                    "SMTP configuration is working. This message was sent by the Gamesphere backend."));
        } catch (MailException ex) {
            throw new BusinessException("Test email could not be sent: " + ex.getMessage(),
                    HttpStatus.BAD_GATEWAY);
        }
    }

    private void send(String to, String subject, String body) {
        if (!emailProperties.isEnabled()) {
            log.debug("Email is disabled. Skipping '{}' email to {}", subject, to);
            return;
        }

        if (!hasText(to)) {
            log.warn("Email skipped because recipient address is blank. Subject: {}", subject);
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("Email is enabled, but JavaMailSender is not configured. Skipping '{}' email to {}", subject, to);
            return;
        }

        try {
            mailSender.send(createMessage(to, subject, body));
        } catch (MailException ex) {
            log.warn("Email could not be sent to {}. Subject: {}", to, subject, ex);
        }
    }

    private SimpleMailMessage createMessage(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        if (hasText(from)) {
            message.setFrom(from);
        }
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        return message;
    }

    private String displayName(User user) {
        if (hasText(user.getFirstName())) {
            return user.getFirstName();
        }
        return user.getUsername();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String trimTrailingSlash(String value) {
        if (!hasText(value)) {
            return "http://localhost:3000";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}

