package io.gudmian.javanote.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author d.guba
 */
public interface NoteRepository {
    Note create(String title,
                String content,
                List<String> tags
    );

    boolean delete(UUID id);

    Optional<Note> update(
            UUID id,
            String title,
            String content,
            List<String> tags
    );

    Optional<Note> read(UUID id);

    List<Note> readAll();
}
