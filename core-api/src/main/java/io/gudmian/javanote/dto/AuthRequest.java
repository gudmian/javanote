package io.gudmian.javanote.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * @author d.guba
 */
public record AuthRequest(
        @NotBlank String username,
        @NotBlank String password
) {
}
