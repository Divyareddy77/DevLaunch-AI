package com.devlaunch.service.impl;

import com.devlaunch.entity.User;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link EmailServiceImpl}.
 * <p>
 * Verifies the password reset email is prepared with the expected subject,
 * recipient, and clickable reset link, and that mail delivery failures are
 * logged and swallowed so the forgot-password contract is preserved.
 * </p>
 *
 * @author DevLaunch
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(
                mailSender, "devlaunch@devlaunch.com",
                "http://localhost:5173", 30L);
    }

    @Test
    @DisplayName("password reset email contains subject, recipient, and reset link")
    void passwordResetEmailContainsTheResetLink() throws Exception {
        final User user = User.builder()
                .firstName("Divya")
                .email("divya@example.com")
                .build();

        emailService.sendPasswordResetEmail(user, "reset-token-123");

        final ArgumentCaptor<MimeMessagePreparator> captor =
                ArgumentCaptor.forClass(MimeMessagePreparator.class);
        verify(mailSender).send(captor.capture());

        final MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()));
        captor.getValue().prepare(message);

        assertEquals("Reset your DevLaunch password", message.getSubject());
        assertEquals("divya@example.com", message.getRecipients(MimeMessage.RecipientType.TO)[0].toString());
        assertEquals("devlaunch@devlaunch.com", message.getFrom()[0].toString());

        // MimeMessageHelper wraps the HTML body in nested multipart
        // containers, so extract the first textual part recursively.
        final String body = extractText(message.getContent());
        assertNotNull(body);
        assertTrue(body.contains("Hello Divya,"));
        assertTrue(body.contains("http://localhost:5173/reset-password?token=reset-token-123"));
        assertTrue(body.contains("The link expires in 30 minutes."));
        assertTrue(body.contains("If you didn't request this, ignore this email."));
    }

    @Test
    @DisplayName("a mail delivery failure is logged and never propagated")
    void mailFailureIsSwallowed() {
        final User user = User.builder()
                .firstName("Divya")
                .email("divya@example.com")
                .build();

        doThrow(new MailException("smtp down") {})
                .when(mailSender).send(any(MimeMessagePreparator.class));

        // Must not throw — the forgot-password endpoint contract depends on it.
        emailService.sendPasswordResetEmail(user, "reset-token-123");
    }

    /**
     * Recursively extracts the first textual part of a MIME body.
     *
     * @param content the raw MIME content
     * @return the text of the first textual part, or {@code null} if none
     */
    private String extractText(final Object content) throws Exception {
        if (content instanceof String text) {
            return text;
        }
        if (content instanceof MimeMultipart multipart) {
            for (int i = 0; i < multipart.getCount(); i++) {
                final String text = extractText(multipart.getBodyPart(i).getContent());
                if (text != null) {
                    return text;
                }
            }
        }
        return null;
    }

}
