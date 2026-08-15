package io.gudmian.javanote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * @author d.guba
 */
public record PushRegisterRequest(
        @NotNull UUID userId,
        @NotBlank String token
) {
}
