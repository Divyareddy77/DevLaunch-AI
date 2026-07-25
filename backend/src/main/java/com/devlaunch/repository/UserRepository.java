package com.devlaunch.repository;

import com.devlaunch.entity.Role;
import com.devlaunch.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link User} entity operations.
 * <p>
 * Provides standard CRUD operations and custom query methods
 * for user-related database access.
 * </p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     *
     * @param email the email address to search for
     * @return an {@link Optional} containing the user if found, or empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether a user with the given email address exists.
     *
     * @param email the email address to check
     * @return {@code true} if a user with the email exists, {@code false} otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Finds all users assigned to a specific role.
     *
     * @param role the role entity to filter by
     * @return a list of users belonging to the specified role
     */
    List<User> findByRole(Role role);

}
