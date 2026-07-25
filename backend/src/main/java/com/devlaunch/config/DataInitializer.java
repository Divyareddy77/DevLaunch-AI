package com.devlaunch.config;

import com.devlaunch.entity.Role;
import com.devlaunch.entity.enums.RoleType;
import com.devlaunch.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Initializes default system data when the application starts.
 * <p>
 * This loader ensures that essential reference data — specifically the
 * default user roles — is present in the database. The operation is
 * <strong>idempotent</strong>: roles that already exist are never
 * inserted again, and existing roles are never altered or removed.
 * </p>
 *
 * @author DevLaunch
 */
@Slf4j
@Configuration
public class DataInitializer {

    private final RoleRepository roleRepository;


    /**
     * Constructs a {@code DataInitializer} with the required repository.
     *
     * @param roleRepository the repository for role persistence
     */
    public DataInitializer(final RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Creates a {@link CommandLineRunner} bean that seeds the default
     * {@link Role roles} into the database at application startup.
     *
     * @return a runner that inserts default roles if they do not already exist
     */
    @Bean
    public CommandLineRunner initDefaultRoles() {
        return args -> {

            // ────────────────────────────────────────────────────────────────
            // 1. STUDENT role
            // ────────────────────────────────────────────────────────────────
            if (!roleRepository.existsByRoleName(RoleType.STUDENT)) {
                Role studentRole = Role.builder()
                        .roleName(RoleType.STUDENT)
                        .build();
                roleRepository.save(studentRole);
                log.info("Inserted default role: {}", RoleType.STUDENT);
            }

            // ────────────────────────────────────────────────────────────────
            // 2. ADMIN role
            // ────────────────────────────────────────────────────────────────
            if (!roleRepository.existsByRoleName(RoleType.ADMIN)) {
                Role adminRole = Role.builder()
                        .roleName(RoleType.ADMIN)
                        .build();
                roleRepository.save(adminRole);
                log.info("Inserted default role: {}", RoleType.ADMIN);
            }

        };
    }

}
