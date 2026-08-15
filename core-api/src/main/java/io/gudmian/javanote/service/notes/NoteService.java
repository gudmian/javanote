package io.gudmian.javanote.service.notes;

import io.gudmian.javanote.data.notes.NoteDocument;
import io.gudmian.javanote.data.user.UserEntity;
import io.gudmian.javanote.domain.notes.NoteRepository;
import io.gudmian.javanote.domain.user.UserRepository;
import io.gudmian.javanote.event.notes.NoteCreatedEvent;
import io.gudmian.javanote.event.notes.NoteEventsTopics;
import io.gudmian.javanote.utils.notes.NoteAccessDeniedException;
import io.gudmian.javanote.utils.notes.NoteNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * @author d.guba
 */
@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final KafkaTemplate<String, NoteCreatedEvent> kafkaTemplate;

    public NoteService(
            NoteRepository noteRepository,
            UserRepository userRepository,
            KafkaTemplate<String, NoteCreatedEvent> kafkaTemplate
    ) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public List<NoteDocument> findAllForOwner(String username) {
        return noteRepository.findAllByOwnerId(resolveUserId(username));
    }

    public NoteDocument findByIdForOwner(UUID noteId, String username) {
        NoteDocument note = noteRepository.findById(noteId)
                .orElseThrow(() -> new NoteNotFoundException(noteId));
        assertOwner(note, username);
        return note;
    }

    @CacheEvict(value = "notesByOwner", key = "#ownerId")
    public NoteDocument create(UUID ownerId, String title, String content, List<String> tags) {
        NoteDocument note = new NoteDocument(
                UUID.randomUUID(), ownerId, title, content, tags, Instant.now(), Instant.now()
        );

        NoteDocument saved = noteRepository.save(note);

        kafkaTemplate.send(
                NoteEventsTopics.NOTE_CREATED,
                ownerId.toString(),
                new NoteCreatedEvent(
                        saved.id(),
                        ownerId,
                        saved.title(),
                        saved.createdAt()
                )
        );

        return saved;
    }

    @Caching(
            put = @CachePut(value = "notes", key = "#noteId"),
            evict = @CacheEvict(value = "notesByOwner", key = "#result.ownerId()")
    )
    public NoteDocument update(UUID noteId, String username, String title, String content, List<String> tags) {
        NoteDocument existing = findByIdForOwner(noteId, username);
        NoteDocument updated = new NoteDocument(
                existing.id(), existing.ownerId(), title, content, tags, existing.createdAt(), Instant.now()
        );
        return noteRepository.save(updated);
    }

    @Caching(evict = {
            @CacheEvict(value = "notes", key = "#noteId"),
            @CacheEvict(value = "notesByOwner", key = "#result.ownerId()")
    })
    public NoteDocument delete(UUID noteId, String username) {
        NoteDocument existing = findByIdForOwner(noteId, username);
        noteRepository.deleteById(noteId);
        return existing;
    }

    public UUID resolveUserId(String username) {
        return userRepository.findByUsername(username)
                .map(UserEntity::getId)
                .orElseThrow();
    }

    private void assertOwner(NoteDocument document, String username) {
        if (!document.ownerId().equals(resolveUserId(username))) {
            throw new NoteAccessDeniedException(document.id());
        }
    }
}
