package com.devlaunch.repository;

import com.devlaunch.entity.Role;
import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    @Query("""
       SELECT u
       FROM User u
       JOIN FETCH u.role
       WHERE u.email = :email
       """)
    Optional<User> findByEmail(@Param("email") String email);

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

    /**
     * Counts the users whose active status matches the given flag.
     *
     * @param isActive whether to count active or inactive users
     * @return the number of users with the matching active status
     */
    long countByIsActive(Boolean isActive);

    /**
     * Finds the ten most recently registered users.
     *
     * @return a list of up to ten users ordered by registration time
     */
    List<User> findTop10ByOrderByCreatedAtDesc();

    /**
     * Searches users with optional free-text search and role/active filters.
     * <p>
     * The free-text search matches the first name, last name, or email
     * (case-insensitive). Passing {@code null} for any filter disables it.
     * The role is fetched eagerly so admin listings do not trigger lazy
     * initialisation errors outside a transaction.
     * </p>
     *
     * @param search   optional free-text search term, or {@code null}
     * @param roleName optional role filter, or {@code null}
     * @param active   optional active-status filter, or {@code null}
     * @param pageable the pagination and sorting information
     * @return a page of matching users
     */
    @Query(value = """
            SELECT u
            FROM User u
            JOIN FETCH u.role r
            WHERE (:search IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:roleName IS NULL OR r.roleName = :roleName)
              AND (:active IS NULL OR u.isActive = :active)
            """,
            countQuery = """
            SELECT COUNT(u)
            FROM User u
            JOIN u.role r
            WHERE (:search IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:roleName IS NULL OR r.roleName = :roleName)
              AND (:active IS NULL OR u.isActive = :active)
            """)
    Page<User> searchUsers(@Param("search") String search,
                           @Param("roleName") RoleType roleName,
                           @Param("active") Boolean active,
                           Pageable pageable);

}
