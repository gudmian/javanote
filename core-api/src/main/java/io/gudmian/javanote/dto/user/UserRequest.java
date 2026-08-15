package io.gudmian.javanote.dto.user;

import jakarta.validation.constraints.NotBlank;

/**
 * @author d.guba
 */
public record UserRequest(
        @NotBlank String username,
        @NotBlank String password
) {
}
