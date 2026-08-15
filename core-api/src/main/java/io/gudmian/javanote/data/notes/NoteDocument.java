package io.gudmian.javanote.data.notes;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * @author d.guba
 */
@Document(collection = "notes")
public record NoteDocument(
        @Id
        UUID id,
        UUID ownerId,
        String title,
        String content,
        List<String> tags,
        Instant createdAt,
        Instant updatedAt
) {
}
