package io.gudmian.javanote.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * @author d.guba
 */
public record NoteRequest(
        @NotBlank
        String title,
        String content,
        List<String> tags
) {
}
