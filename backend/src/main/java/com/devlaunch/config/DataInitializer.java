package com.devlaunch.config;

import com.devlaunch.entity.ResumeTemplate;
import com.devlaunch.entity.Role;
import com.devlaunch.entity.enums.RoleType;
import com.devlaunch.repository.ResumeTemplateRepository;
import com.devlaunch.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


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

    private final RoleRepository roleRepository;
    private final ResumeTemplateRepository resumeTemplateRepository;


    /**
     * Constructs a {@code DataInitializer} with the required repositories.
     *
     * @param roleRepository         the repository for role persistence
     * @param resumeTemplateRepository the repository for resume template persistence
     */
    public DataInitializer(final RoleRepository roleRepository,
                           final ResumeTemplateRepository resumeTemplateRepository) {
        this.roleRepository = roleRepository;
        this.resumeTemplateRepository = resumeTemplateRepository;
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

}
