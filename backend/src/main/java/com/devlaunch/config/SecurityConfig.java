package com.devlaunch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Central security configuration for the DevLaunch API.
 * <p>
 * Configures HTTP security, password encoding, and authentication management.
 * JWT filter integration is prepared via a placeholder comment and will be
 * wired in once the JwtAuthenticationFilter is fully implemented.
 * </p>
 *
 * @author DevLaunch
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/**",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    /**
     * Configures the HTTP security filter chain.
     * <p>
     * CSRF is disabled for stateless REST API access. Public endpoints
     * (authentication, Swagger) permit all requests; all other endpoints
     * require authentication. Session management is stateless.
     * </p>
     * <p>
     * TODO: Insert JwtAuthenticationFilter before UsernamePasswordAuthenticationFilter
     * once the JWT utility and token parsing logic are implemented.
     * </p>
     *
     * @param http the {@link HttpSecurity} to configure
     * @return the built {@link SecurityFilterChain}
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
                );

        // TODO: Add JwtAuthenticationFilter to the filter chain
        // http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Provides a BCrypt-based password encoder for securely hashing passwords.
     *
     * @return the {@link PasswordEncoder} instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes the {@link AuthenticationManager} from the Spring Security
     * configuration for use in the authentication service.
     *
     * @param authenticationConfiguration the auto-configured authentication configuration
     * @return the {@link AuthenticationManager} bean
     * @throws Exception if the authentication manager cannot be obtained
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}
