package com.devlaunch.mapper;

import com.devlaunch.dto.request.RegisterRequest;
import com.devlaunch.dto.response.UserResponse;
import com.devlaunch.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for authentication-related object conversions.
 * <p>
 * Handles mapping between {@link RegisterRequest} DTO and {@link User} entity,
 * as well as between {@link User} entity and {@link UserResponse} DTO.
 * Password encoding is intentionally excluded — it belongs in the Service layer.
 * </p>
 *
 * @author DevLaunch
 */
@Mapper(componentModel = "spring")
public interface AuthMapper {

    /**
     * Maps a registration request DTO to a User entity.
     * <p>
     * Fields with matching names (firstName, lastName, email, password, phone)
     * are auto-mapped. Fields that are controlled by the system (role, isActive)
     * are explicitly ignored. Inherited fields (id, createdAt, updatedAt) are
     * not exposed by Lombok's builder and are therefore excluded by default.
     * </p>
     *
     * @param request the registration request containing user details
     * @return a new User entity with the mapped fields
     */
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    User toUser(RegisterRequest request);

    /**
     * Maps a User entity to a user response DTO.
     * <p>
     * The role name is extracted from the {@code Role} entity's {@code roleName}
     * enum and converted to its string representation. Password and timestamp
     * fields are excluded from the response for security purposes.
     * </p>
     *
     * @param user the user entity to map from
     * @return a user response DTO with safe, non-sensitive data
     */
    @Mapping(target = "role", expression = "java(user.getRole().getRoleName().name())")
    UserResponse toUserResponse(User user);

}
