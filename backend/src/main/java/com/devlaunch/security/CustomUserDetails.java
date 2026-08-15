package com.devlaunch.security;

import com.devlaunch.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom implementation of {@link UserDetails} that wraps the {@link User} entity.
 * <p>
 * Maps the entity's email to the username, uses the hashed password for
 * authentication, and derives the granted authority from the user's role.
 * The {@code isActive} field controls whether the account is enabled.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;

    /**
     * Constructs a {@code CustomUserDetails} wrapping the given user entity.
     *
     * @param user the user entity to wrap
     */
    public CustomUserDetails(User user) {
        this.user = user;
    }

    /**
     * Returns the authorities granted to the user based on their role.
     * The role's enum name is prefixed with {@code ROLE_} to conform to
     * Spring Security's role-based authority conventions.
     *
     * @return a singleton collection containing the role-based authority
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName().name())
        );
    }

    /**
     * Returns the hashed password for authentication.
     *
     * @return the hashed password
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Returns the email address used as the authentication principal.
     *
     * @return the user's email
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    /**
     * Indicates whether the user's account has not expired.
     *
     * @return {@code true} — account expiry is not enforced
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user's account is not locked.
     *
     * @return {@code true} — account locking is not enforced
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials have not expired.
     *
     * @return {@code true} — credential expiry is not enforced
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled.
     * <p>
     * Maps directly to the entity's {@code isActive} flag to allow
     * administrators to deactivate user accounts.
     * </p>
     *
     * @return {@code true} if the user is active, {@code false} otherwise
     */
    @Override
    public boolean isEnabled() {
        return user.getIsActive();
    }

}
