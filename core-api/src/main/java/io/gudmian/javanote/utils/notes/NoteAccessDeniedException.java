package io.gudmian.javanote.utils.notes;

import java.util.UUID;

/**
 * @author d.guba
 */
public class NoteAccessDeniedException extends RuntimeException {
    public NoteAccessDeniedException(UUID noteId) {
        super("Access denied to note: " + noteId);
    }
}
