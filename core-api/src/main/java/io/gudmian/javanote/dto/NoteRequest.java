package io.gudmian.javanote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * @author d.guba
 */
public record NoteRequest(
        @NotNull
        UUID ownerId,
        @NotBlank
        String title,
        String content,
        List<String> tags
) {
}
