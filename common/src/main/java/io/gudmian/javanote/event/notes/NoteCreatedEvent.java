package io.gudmian.javanote.event.notes;

import java.time.Instant;
import java.util.UUID;

/**
 * @author d.guba
 */
public record NoteCreatedEvent(
        UUID noteId,
        UUID ownerId,
        String title,
        Instant createdAt
) {
}