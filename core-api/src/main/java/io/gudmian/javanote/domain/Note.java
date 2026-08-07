package io.gudmian.javanote.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * @author d.guba
 */
public record Note(
        UUID id,
        String title,
        String content,
        List<String> tags,
        Instant createdAt,
        Instant updatedAt
) {
    public Note {
        tags = List.copyOf(tags);
    }
}
