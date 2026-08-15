package io.gudmian.javanote.dto.notes;

import io.gudmian.javanote.data.notes.NoteDocument;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * @author d.guba
 */
public record NoteResponse(
        UUID id,
        UUID ownerId,
        String title,
        String content,
        List<String> tags,
        Instant createdAt,
        Instant updatedAt
) {
    public static NoteResponse from(NoteDocument note) {
        return new NoteResponse(
                note.id(),
                note.ownerId(),
                note.title(),
                note.content(),
                note.tags(),
                note.createdAt(),
                note.updatedAt()
        );
    }
}
