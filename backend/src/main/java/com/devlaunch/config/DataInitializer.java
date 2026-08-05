package com.devlaunch.config;

import com.devlaunch.entity.ResumeTemplate;
import com.devlaunch.entity.Role;
import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.RoleType;
import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.repository.ResumeTemplateRepository;
import com.devlaunch.repository.RoleRepository;
import com.devlaunch.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * Initializes default system data when the application starts.
 * <p>
 * This loader ensures that essential reference data — specifically the
 * default user roles and predefined resume templates — is present in the
 * database. The operation is <strong>idempotent</strong>: existing records
 * are never inserted again, and existing records are never altered or removed.
 * </p>
 *
 * @author DevLaunch
 */
@Slf4j
@Configuration
public class DataInitializer {

    /**
     * The email of the default administrator account created on first startup.
     */
    public static final String DEFAULT_ADMIN_EMAIL = "admin@devlaunch.com";

    /**
     * The password of the default administrator account created on first startup.
     */
    public static final String DEFAULT_ADMIN_PASSWORD = "Admin@123456";

    private final RoleRepository roleRepository;
    private final ResumeTemplateRepository resumeTemplateRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    /**
     * Constructs a {@code DataInitializer} with the required repositories.
     *
     * @param roleRepository           the repository for role persistence
     * @param resumeTemplateRepository the repository for resume template persistence
     * @param userRepository           the repository for user persistence
     * @param passwordEncoder          the encoder used to hash the admin password
     */
    public DataInitializer(final RoleRepository roleRepository,
                           final ResumeTemplateRepository resumeTemplateRepository,
                           final UserRepository userRepository,
                           final PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.resumeTemplateRepository = resumeTemplateRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a {@link CommandLineRunner} bean that seeds the default
     * {@link Role roles} into the database at application startup.
     *
     * @return a runner that inserts default roles if they do not already exist
     */
    @Bean
    @Order(1)
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

    /**
     * Creates a {@link CommandLineRunner} bean that seeds the default
     * {@link ResumeTemplate resume templates} into the database at
     * application startup.
     * <p>
     * Templates are predefined by the system and provide different visual
     * styles (Professional, Modern, Minimal, Creative) that users can apply
     * to their resumes. The operation is idempotent — templates that already
     * exist are not inserted again.
     * </p>
     *
     * @return a runner that inserts default templates if they do not already exist
     */
    @Bean
    @Order(2)
    public CommandLineRunner initDefaultTemplates() {
        return args -> {

            // ────────────────────────────────────────────────────────────────
            // 1. Professional template
            // ────────────────────────────────────────────────────────────────
            if (!resumeTemplateRepository.existsByName("Professional")) {
                ResumeTemplate professional = ResumeTemplate.builder()
                        .name("Professional")
                        .description("A clean and formal layout suitable for corporate and traditional industries.")
                        .previewImageUrl(null)
                        .build();
                resumeTemplateRepository.save(professional);
                log.info("Inserted default template: Professional");
            }

            // ────────────────────────────────────────────────────────────────
            // 2. Modern template
            // ────────────────────────────────────────────────────────────────
            if (!resumeTemplateRepository.existsByName("Modern")) {
                ResumeTemplate modern = ResumeTemplate.builder()
                        .name("Modern")
                        .description("A contemporary design with a sidebar accent, ideal for tech and creative roles.")
                        .previewImageUrl(null)
                        .build();
                resumeTemplateRepository.save(modern);
                log.info("Inserted default template: Modern");
            }

            // ────────────────────────────────────────────────────────────────
            // 3. Minimal template
            // ────────────────────────────────────────────────────────────────
            if (!resumeTemplateRepository.existsByName("Minimal")) {
                ResumeTemplate minimal = ResumeTemplate.builder()
                        .name("Minimal")
                        .description("A simple, uncluttered layout with plenty of white space for a refined look.")
                        .previewImageUrl(null)
                        .build();
                resumeTemplateRepository.save(minimal);
                log.info("Inserted default template: Minimal");
            }

            // ────────────────────────────────────────────────────────────────
            // 4. Creative template
            // ────────────────────────────────────────────────────────────────
            if (!resumeTemplateRepository.existsByName("Creative")) {
                ResumeTemplate creative = ResumeTemplate.builder()
                        .name("Creative")
                        .description("A bold and expressive design for design, marketing, and media professionals.")
                        .previewImageUrl(null)
                        .build();
                resumeTemplateRepository.save(creative);
                log.info("Inserted default template: Creative");
            }

        };
    }

    /**
     * Creates a {@link CommandLineRunner} bean that seeds a default
     * administrator account at application startup.
     * <p>
     * The account is created only on the first startup when no user with the
     * admin email exists, so the operation is idempotent. The credentials are
     * printed to the log so the admin panel can be accessed immediately. For
     * production deployments the password should be changed after the first
     * login.
     * </p>
     *
     * @return a runner that inserts the default admin user if it does not exist
     */
    @Bean
    @Order(3)
    public CommandLineRunner initDefaultAdmin() {
        return args -> {
            if (userRepository.existsByEmail(DEFAULT_ADMIN_EMAIL)) {
                return;
            }

            final Role adminRole = roleRepository.findByRoleName(RoleType.ADMIN)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Role ADMIN not found in the system"));

            final User admin = User.builder()
                    .firstName("DevLaunch")
                    .lastName("Admin")
                    .email(DEFAULT_ADMIN_EMAIL)
                    .password(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD))
                    .phone("0000000000")
                    .isActive(true)
                    .role(adminRole)
                    .build();
            userRepository.save(admin);

            log.info("Created default admin user: {} / {}",
                    DEFAULT_ADMIN_EMAIL, DEFAULT_ADMIN_PASSWORD);
        };
    }

}
