package io.gudmian.javanote.rest;

import io.gudmian.javanote.data.NoteDocument;
import io.gudmian.javanote.data.UserEntity;
import io.gudmian.javanote.domain.NoteRepository;
import io.gudmian.javanote.domain.UserRepository;
import io.gudmian.javanote.dto.NoteRequest;
import io.gudmian.javanote.dto.NoteResponse;
import io.gudmian.javanote.utils.NoteAccessDeniedException;
import io.gudmian.javanote.utils.NoteNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * @author d.guba
 */
@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public NoteController(
            NoteRepository noteRepository,
            UserRepository userRepository
    ) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<NoteResponse> readAll(Authentication authentication) {
        UUID userId = userRepository.findByUsername(authentication.getName()).map(UserEntity::getId)
                .orElseThrow();

        return noteRepository.findAllByOwnerId(userId).stream()
                .map(NoteResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteResponse> read(@PathVariable UUID id, Authentication authentication) {
        NoteDocument document = noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException(id));

        assertOwner(document, authentication);

        return ResponseEntity.ok(NoteResponse.from(document));
    }

    @PostMapping
    public ResponseEntity<NoteResponse> create(@Valid @RequestBody NoteRequest request) {
        NoteDocument document = new NoteDocument(
                UUID.randomUUID(),
                request.ownerId(),
                request.title(),
                request.content(),
                request.tags(),
                Instant.now(),
                Instant.now()
        );
        NoteDocument created = noteRepository.save(document);
        NoteResponse response = NoteResponse.from(created);
        return ResponseEntity.created(URI.create("/api/notes/" + created.id())).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody NoteRequest request,
            Authentication authentication
    ) {
        NoteDocument existing = noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException(id));

        assertOwner(existing, authentication);

        NoteDocument document = new NoteDocument(
                existing.id(),
                existing.ownerId(),
                request.title(),
                request.content(),
                request.tags(),
                existing.createdAt(),
                Instant.now()
        );
        NoteDocument updated = noteRepository.save(document);

        return ResponseEntity.ok(NoteResponse.from(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication authentication) {
        NoteDocument document = noteRepository.findById(id).orElseThrow(
                () -> new NoteNotFoundException(id)
        );

        assertOwner(document, authentication);

        noteRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    private void assertOwner(NoteDocument document, Authentication auth) {
        UUID userId = userRepository.findByUsername(auth.getName()).map(UserEntity::getId).orElseThrow();

        if (!document.ownerId().equals(userId)) {
            throw new NoteAccessDeniedException(document.id());
        }
    }
}
