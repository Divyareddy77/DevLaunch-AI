package com.devlaunch.controller;

import com.devlaunch.dto.response.MessageResponse;
import com.devlaunch.dto.response.UserResponse;
import com.devlaunch.service.interfaces.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller tests for the register, forgot-password and reset-password
 * endpoints.
 * <p>
 * Verifies request validation on all endpoints — including the shared
 * strong-password policy enforced by {@link RegisterRequest} and
 * {@link ResetPasswordRequest} — and the success responses for valid
 * payloads.
 * </p>
 *
 * @author DevLaunch
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService)).build();
    }

    @Test
    @DisplayName("register returns 201 Created for a payload with a strong password")
    void registerAcceptsStrongPassword() throws Exception {
        when(authService.register(any()))
                .thenReturn(UserResponse.builder()
                        .id(1L)
                        .firstName("Divya")
                        .lastName("Reddy")
                        .email("divya@example.com")
                        .phone("+1 555 123 4567")
                        .role("USER")
                        .isActive(true)
                        .build());

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {
                                  "firstName": "Divya",
                                  "lastName": "Reddy",
                                  "email": "divya@example.com",
                                  "password": "Password@123",
                                  "phone": "+1 555 123 4567"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("divya@example.com"));
    }

    @Test
    @DisplayName("register rejects a weak password")
    void registerRejectsWeakPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {
                                  "firstName": "Divya",
                                  "lastName": "Reddy",
                                  "email": "divya@example.com",
                                  "password": "divya123",
                                  "phone": "+1 555 123 4567"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("register rejects a missing password")
    void registerRejectsMissingPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {
                                  "firstName": "Divya",
                                  "lastName": "Reddy",
                                  "email": "divya@example.com",
                                  "phone": "+1 555 123 4567"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("forgot-password returns the generic confirmation message")
    void forgotPasswordReturnsGenericMessage() throws Exception {
        when(authService.forgotPassword(any()))
                .thenReturn(new MessageResponse("If an account exists, a password reset link has been sent."));

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType("application/json")
                        .content("""
                                {"email": "user@example.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("If an account exists, a password reset link has been sent."));
    }

    @Test
    @DisplayName("forgot-password rejects an invalid email format")
    void forgotPasswordRejectsInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType("application/json")
                        .content("""
                                {"email": "not-an-email"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("forgot-password rejects a blank email")
    void forgotPasswordRejectsBlankEmail() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType("application/json")
                        .content("""
                                {"email": ""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("reset-password returns a confirmation message for a valid payload")
    void resetPasswordAcceptsValidPayload() throws Exception {
        when(authService.resetPassword(any()))
                .thenReturn(new MessageResponse("Your password has been updated successfully."));

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType("application/json")
                        .content("""
                                {
                                  "token": "abc123",
                                  "newPassword": "Password@123",
                                  "confirmPassword": "Password@123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Your password has been updated successfully."));
    }

    @Test
    @DisplayName("reset-password rejects a weak password")
    void resetPasswordRejectsWeakPassword() throws Exception {
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType("application/json")
                        .content("""
                                {
                                  "token": "abc123",
                                  "newPassword": "weak",
                                  "confirmPassword": "weak"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("reset-password rejects a missing token")
    void resetPasswordRejectsMissingToken() throws Exception {
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType("application/json")
                        .content("""
                                {
                                  "newPassword": "Password@123",
                                  "confirmPassword": "Password@123"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

}
