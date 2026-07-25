package com.devlaunch.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) configuration for the DevLaunch Backend API.
 * <p>
 * This class exposes a single {@link OpenAPI} bean that customises the
 * auto-configured Springdoc OpenAPI instance with:
 * <ul>
 *   <li>API metadata (title, version, description)</li>
 *   <li>JWT Bearer authentication scheme</li>
 *   <li>Global security requirement so every protected endpoint
 *       automatically includes the Bearer token input in Swagger UI</li>
 * </ul>
 * </p>
 *
 * <p><b>Usage in Swagger UI:</b>
 * Click the <em>Authorize</em> button and paste a valid JWT token.
 * All subsequent requests include the {@code Authorization: Bearer <token>}
 * header automatically.
 * </p>
 *
 * @see <a href="https://springdoc.org/">Springdoc OpenAPI Documentation</a>
 * @see <a href="https://swagger.io/specification/">OpenAPI Specification</a>
 */
@Configuration
public class OpenApiConfig {

    /** Human-readable title of the API. */
    private static final String API_TITLE = "DevLaunch Backend API";

    /** Current API version. */
    private static final String API_VERSION = "v1.0";

    /** Short description of the API's purpose. */
    private static final String API_DESCRIPTION =
            "REST APIs for the DevLaunch AI-Powered Developer Career Hub.";

    /** Name of the JWT Bearer security scheme as displayed in Swagger UI. */
    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    /**
     * Builds and exposes the customised {@link OpenAPI} bean.
     * <p>
     * The bean is picked up automatically by Springdoc and used to
     * populate the OpenAPI specification served at {@code /v3/api-docs}
     * and the Swagger UI at {@code /swagger-ui.html}.
     * </p>
     *
     * @return a fully configured {@link OpenAPI} instance
     */
    @Bean
    public OpenAPI devLaunchOpenAPI() {
        return new OpenAPI()
                .info(buildApiInfo())
                .components(buildComponents())
                .addSecurityItem(buildGlobalSecurityRequirement());
    }

    /**
     * Builds the API metadata section.
     *
     * @return the {@link Info} object with title, version, and description
     */
    private Info buildApiInfo() {
        return new Info()
                .title(API_TITLE)
                .version(API_VERSION)
                .description(API_DESCRIPTION)
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0"));
    }

    /**
     * Builds the reusable components section containing the JWT Bearer
     * security scheme definition.
     *
     * @return the {@link Components} object with the security scheme
     */
    private Components buildComponents() {
        return new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME, buildSecurityScheme());
    }

    /**
     * Builds the JWT Bearer {@link SecurityScheme}.
     * <p>
     * The scheme is of type {@link SecurityScheme.Type#HTTP HTTP} with
     * scheme {@code bearer} and format {@code JWT}. Swagger UI uses this
     * definition to display the Authorize button and to send the
     * {@code Authorization: Bearer <token>} header on protected requests.
     * </p>
     *
     * @return the configured {@link SecurityScheme}
     */
    private SecurityScheme buildSecurityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter your JWT Bearer token below. " +
                        "The token is typically obtained from the " +
                        "<strong>/api/auth/login</strong> endpoint.");
    }

    /**
     * Builds a global {@link SecurityRequirement} that applies the JWT
     * Bearer scheme to every endpoint by default.
     * <p>
     * This configuration enables the Authorize button in Swagger UI and
     * allows authenticated requests to automatically include the
     * Authorization header. Public endpoints remain accessible because
     * they are explicitly permitted in the application's Spring Security
     * configuration.
     * </p>
     *
     * @return the global {@link SecurityRequirement}
     */
    private SecurityRequirement buildGlobalSecurityRequirement() {
        return new SecurityRequirement().addList(SECURITY_SCHEME_NAME);
    }

}
