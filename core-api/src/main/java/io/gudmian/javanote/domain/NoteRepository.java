package io.gudmian.javanote.domain;

import io.gudmian.javanote.data.NoteDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

/**
 * @author d.guba
 */
public interface NoteRepository extends MongoRepository<NoteDocument, UUID> {
    List<NoteDocument> findAllByOwnerId(UUID ownerId);
}
