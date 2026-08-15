package com.devlaunch.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Service for handling JSON Web Token (JWT) operations.
 * <p>
 * Provides methods for generating, parsing, and validating JWT tokens
 * used for stateless authentication. Tokens are signed using the
 * HMAC-SHA256 algorithm with a configurable secret key and expiration.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class JwtService {

    private final String jwtSecret;
    private final long jwtExpiration;

    /**
     * Constructs a {@code JwtService} with the configured secret and expiration.
     *
     * @param jwtSecret     the secret key used for signing tokens (injected from {@code jwt.secret})
     * @param jwtExpiration the token expiration duration in milliseconds (injected from {@code jwt.expiration})
     */
    public JwtService(@Value("${jwt.secret}") final String jwtSecret,
                      @Value("${jwt.expiration}") final long jwtExpiration) {
        this.jwtSecret = jwtSecret;
        this.jwtExpiration = jwtExpiration;
    }

    /**
     * Generates a JWT token for the given user details.
     * <p>
     * The token's subject is set to the username (email) obtained from
     * {@link UserDetails#getUsername()}. The token is signed with the
     * configured HMAC-SHA256 key and expires after the configured duration.
     * </p>
     *
     * @param userDetails the authenticated user's details
     * @return a signed JWT token string
     */
    public String generateToken(final UserDetails userDetails) {
        final Date now = new Date();
        final Date expiration = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extracts the username (subject) from the given JWT token.
     *
     * @param token the JWT token
     * @return the subject claim (username/email) contained in the token
     */
    public String extractUsername(final String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Validates the given JWT token against the provided user details.
     * <p>
     * Checks that the token's subject matches the username and that the
     * token has not expired.
     * </p>
     *
     * @param token       the JWT token to validate
     * @param userDetails the user details to validate against
     * @return {@code true} if the token is valid, {@code false} otherwise
     */
    public boolean isTokenValid(final String token, final UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Checks whether the given JWT token has expired.
     *
     * @param token the JWT token to check
     * @return {@code true} if the token has expired, {@code false} otherwise
     */
    public boolean isTokenExpired(final String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from the given JWT token.
     *
     * @param token the JWT token
     * @return the expiration date claim
     */
    private Date extractExpiration(final String token) {
        return extractAllClaims(token).getExpiration();
    }

    /**
     * Parses the given JWT token and returns its claims.
     *
     * @param token the JWT token
     * @return the claims contained in the token
     */
    private Claims extractAllClaims(final String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Creates and returns the HMAC-SHA256 signing key from the configured secret.
     * <p>
     * The secret string is decoded as UTF-8 bytes and used to generate
     * a {@link SecretKey} via the {@code Keys.hmacShaKeyFor} method.
     * </p>
     *
     * @return the HMAC-SHA256 {@link SecretKey}
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

}
