package io.gudmian.javanote.utils.notes;

import java.util.UUID;

/**
 * @author d.guba
 */
public class NoteNotFoundException extends RuntimeException {

    public NoteNotFoundException(UUID id) {
        super("Note not found for id: " + id);
    }
}
