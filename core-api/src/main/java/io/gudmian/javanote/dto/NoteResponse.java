package io.gudmian.javanote.dto;

import io.gudmian.javanote.domain.Note;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * @author d.guba
 */
public record NoteResponse(
        UUID id,
        String title,
        String content,
        List<String> tags,
        Instant createdAt,
        Instant updatedAt
) {
    public static NoteResponse from(Note note) {
        return new NoteResponse(
                note.id(),
                note.title(),
                note.content(),
                note.tags(),
                note.createdAt(),
                note.updatedAt()
        );
    }
}
