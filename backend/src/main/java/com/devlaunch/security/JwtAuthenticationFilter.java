package com.devlaunch.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter that intercepts incoming HTTP requests,
 * extracts and validates JWT tokens, and populates the Spring Security
 * {@link SecurityContextHolder} with the authenticated principal.
 * <p>
 * Extends {@link OncePerRequestFilter} to guarantee a single execution
 * per request dispatch. Delegates token parsing and validation to
 * {@link JwtService} and user loading to {@link CustomUserDetailsService}.
 * </p>
 *
 * @author DevLaunch
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = BEARER_PREFIX.length();

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Constructs the filter with the required dependencies.
     *
     * @param jwtService               the service for JWT token operations
     * @param customUserDetailsService the service for loading user details
     */
    public JwtAuthenticationFilter(final JwtService jwtService,
                                   final CustomUserDetailsService customUserDetailsService) {
        this.jwtService = jwtService;
        this.customUserDetailsService = customUserDetailsService;
    }

    /**
     * Filters each incoming HTTP request.
     * <p>
     * Extracts the JWT token from the Authorization header, validates it,
     * loads the corresponding user details, and sets the authentication
     * in the security context. If the token is missing, invalid, or the
     * user cannot be found, the filter chain continues without setting
     * an authentication — allowing Spring Security to handle access denial.
     * </p>
     *
     * @param request     the incoming HTTP request
     * @param response    the outgoing HTTP response
     * @param filterChain the filter chain to pass the request along
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            @NonNull final HttpServletRequest request,
            @NonNull final HttpServletResponse response,
            @NonNull final FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        // Skip authentication if the header is missing or does not use Bearer scheme
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract the JWT token from the Bearer header
        final String jwt = authHeader.substring(BEARER_PREFIX_LENGTH);

        // Extract the username (email) from the token
        final String username = jwtService.extractUsername(jwt);

        // Proceed only if we have a username and no authentication is already set
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            final UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                final UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

}
