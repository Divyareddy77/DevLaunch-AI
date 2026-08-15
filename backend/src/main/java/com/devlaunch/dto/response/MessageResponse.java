package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Generic message response DTO.
 * <p>
 * Used by endpoints that do not return a payload, such as the
 * forgot-password and reset-password flows, where the client only
 * needs a human-readable confirmation message.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {

    private String message;

}
