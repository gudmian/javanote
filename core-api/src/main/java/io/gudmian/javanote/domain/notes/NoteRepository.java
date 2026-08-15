package io.gudmian.javanote.domain.notes;

import io.gudmian.javanote.data.notes.NoteDocument;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author d.guba
 */
public interface NoteRepository extends MongoRepository<NoteDocument, UUID> {

    @Cacheable("notes")
    Optional<NoteDocument> findById(UUID id);

    @Cacheable("notesByOwner")
    List<NoteDocument> findAllByOwnerId(UUID ownerId);
}
