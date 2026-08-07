package io.gudmian.javanote.data;

import io.gudmian.javanote.domain.Note;
import io.gudmian.javanote.domain.NoteRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author d.guba
 */
@Repository
public class InMemoryNoteRepository implements NoteRepository {
    private final ConcurrentHashMap<UUID, Note> storage = new ConcurrentHashMap<>();

    @Override
    public Note create(
            String title,
            String content,
            List<String> tags
    ) {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Note note = new Note(
                id,
                title,
                content,
                tags,
                now,
                now
        );
        storage.put(id, note);
        return note;
    }

    @Override
    public boolean delete(UUID id) {
        Note deleted = storage.remove(id);
        return deleted != null;
    }

    @Override
    public Optional<Note> update(
            UUID id,
            String title,
            String content,
            List<String> tags
    ) {
        Note updated = storage.computeIfPresent(id, (key, existing) ->
                new Note(existing.id(), title, content, tags, existing.createdAt(), Instant.now()));
        return Optional.ofNullable(updated);
    }

    @Override
    public Optional<Note> read(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Note> readAll() {
        return new ArrayList<>(storage.values());
    }
}
