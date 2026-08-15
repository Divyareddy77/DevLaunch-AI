package com.devlaunch.repository;

import com.devlaunch.entity.Role;
import com.devlaunch.entity.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for {@link Role} entity operations.
 * <p>
 * Provides standard CRUD operations and custom query methods
 * for role-related database access.
 * </p>
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Finds a role by its {@link RoleType} name.
     *
     * @param roleType the role type to search for
     * @return an {@link Optional} containing the role if found, or empty otherwise
     */
    Optional<Role> findByRoleName(RoleType roleType);

    /**
     * Checks whether a role with the given {@link RoleType} exists.
     *
     * @param roleType the role type to check
     * @return {@code true} if a role with the specified type exists, {@code false} otherwise
     */
    boolean existsByRoleName(RoleType roleType);

}
