package com.devlaunch.service.impl;

import com.devlaunch.entity.User;
import com.devlaunch.service.interfaces.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * SMTP-backed implementation of {@link EmailService}.
 * <p>
 * Delivers the password reset email using the configured {@link JavaMailSender}.
 * The reset link is built from the configured frontend base URL and the one-time
 * token; the email body includes the recipient's first name and the token expiry
 * window. Any {@link MailException} is logged and swallowed so a delivery failure
 * never reveals whether an account exists — the forgot-password contract requires
 * an identical response either way.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String frontendBaseUrl;
    private final long resetTokenExpiryMinutes;

    /**
     * Constructs the email service with the mail sender and configuration.
     *
     * @param mailSender              the configured Spring mail sender
     * @param fromAddress             the "from" address used on outgoing mail
     * @param frontendBaseUrl         the frontend base URL for reset links
     * @param resetTokenExpiryMinutes how long reset tokens remain valid
     */
    public EmailServiceImpl(final JavaMailSender mailSender,
                            @Value("${spring.mail.from}") final String fromAddress,
                            @Value("${devlaunch.auth.frontend-base-url}") final String frontendBaseUrl,
                            @Value("${devlaunch.auth.reset-token-expiry-minutes}") final long resetTokenExpiryMinutes) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.frontendBaseUrl = frontendBaseUrl;
        this.resetTokenExpiryMinutes = resetTokenExpiryMinutes;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Runs asynchronously so a slow SMTP round-trip cannot make the
     * forgot-password response measurably slower for registered emails
     * than for unregistered ones (which would leak account existence
     * through response timing). Delivery failures are logged and the
     * caller always receives the generic confirmation.
     * </p>
     */
    @Override
    @Async
    public void sendPasswordResetEmail(final User user, final String resetToken) {
        final String resetUrl = frontendBaseUrl + "/reset-password?token=" + resetToken;

        final MimeMessagePreparator preparator = mimeMessage -> {
            final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(user.getEmail());
            helper.setSubject("Reset your DevLaunch password");
            helper.setText(buildHtmlBody(user, resetUrl), true);
        };

        try {
            mailSender.send(preparator);
            log.info("Password reset email sent to user id={}", user.getId());
        } catch (final MailException ex) {
            // Never break the forgot-password flow because mail delivery
            // failed; the endpoint contract requires a generic response.
            log.warn("Failed to send password reset email to user id={}: {}",
                    user.getId(), ex.getMessage());
        }
    }

    /**
     * Builds the HTML body of the password reset email.
     *
     * @param user     the recipient
     * @param resetUrl the full reset link
     * @return the HTML email body
     */
    private String buildHtmlBody(final User user, final String resetUrl) {
        return "<p>Hello " + escapeHtml(user.getFirstName()) + ",</p>"
                + "<p>We received a request to reset your password.</p>"
                + "<p>Click below:</p>"
                + "<p><a href=\"" + resetUrl + "\">" + resetUrl + "</a></p>"
                + "<p>The link expires in " + resetTokenExpiryMinutes + " minutes.</p>"
                + "<p>If you didn't request this, ignore this email.</p>";
    }

    /**
     * Escapes HTML special characters in user-provided text so it is safe
     * to embed in the email body.
     *
     * @param value the raw text
     * @return the escaped text
     */
    private String escapeHtml(final String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

}
