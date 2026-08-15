package com.devlaunch.security;

import com.devlaunch.entity.User;
import com.devlaunch.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link UserDetailsService} that loads user data
 * from the database using the {@link UserRepository}.
 * <p>
 * Uses the email address as the authentication principal, which is
 * treated as the username by Spring Security.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Constructs the service with the required repository dependency.
     *
     * @param userRepository the repository for user data access
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by their email address.
     * <p>
     * The method is marked {@code @Transactional(readOnly = true)} to
     * ensure the user's lazy-loaded {@code role} relationship is available
     * when building the granted authorities.
     * </p>
     *
     * @param email the email address of the user to load
     * @return a {@link CustomUserDetails} wrapping the found user entity
     * @throws UsernameNotFoundException if no user is found with the given email
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));

        return new CustomUserDetails(user);
    }

}
