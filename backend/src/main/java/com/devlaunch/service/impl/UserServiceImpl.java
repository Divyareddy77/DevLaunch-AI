package com.devlaunch.service.impl;

import com.devlaunch.dto.request.UpdateUserRequest;
import com.devlaunch.dto.response.UserResponse;
import com.devlaunch.entity.User;
import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.mapper.AuthMapper;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.service.interfaces.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link UserService} providing user profile
 * retrieval and update operations for the currently authenticated user.
 * <p>
 * Uses the Spring Security {@link SecurityContextHolder} to obtain
 * the authenticated user's email, then delegates persistence and
 * mapping to {@link UserRepository} and {@link AuthMapper}
 * respectively.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthMapper authMapper;

    /**
     * Constructs the user service with the required dependencies.
     *
     * @param userRepository repository for user data access
     * @param authMapper     mapper for entity-to-DTO conversion
     */
    public UserServiceImpl(final UserRepository userRepository,
                           final AuthMapper authMapper) {
        this.userRepository = userRepository;
        this.authMapper = authMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        final User user = getAuthenticatedUser();
        return authMapper.toUserResponse(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserResponse updateCurrentUser(final UpdateUserRequest request) {
        final User user = getAuthenticatedUser();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        final User savedUser = userRepository.save(user);
        return authMapper.toUserResponse(savedUser);
    }

    /**
     * Retrieves the currently authenticated user from the database.
     * <p>
     * Extracts the username (email) from the {@link SecurityContextHolder},
     * fetches the corresponding {@link User} entity from the repository,
     * and throws a {@link ResourceNotFoundException} if no matching user
     * is found.
     * </p>
     *
     * @return the authenticated {@link User} entity
     * @throws ResourceNotFoundException if the user is not found in the database
     */
    private User getAuthenticatedUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with email " + email + " not found"));
    }

}
