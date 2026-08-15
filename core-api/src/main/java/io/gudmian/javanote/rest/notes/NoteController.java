package io.gudmian.javanote.rest.notes;

import io.gudmian.javanote.data.notes.NoteDocument;
import io.gudmian.javanote.dto.notes.NoteRequest;
import io.gudmian.javanote.dto.notes.NoteResponse;
import io.gudmian.javanote.service.notes.NoteService;
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
import java.util.List;
import java.util.UUID;

/**
 * @author d.guba
 */
@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public List<NoteResponse> readAll(Authentication authentication) {
        return noteService.findAllForOwner(authentication.getName()).stream()
                .map(NoteResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteResponse> read(@PathVariable UUID id, Authentication authentication) {
        return ResponseEntity.ok(NoteResponse.from(noteService.findByIdForOwner(id, authentication.getName())));
    }

    @PostMapping
    public ResponseEntity<NoteResponse> create(@Valid @RequestBody NoteRequest request) {
        NoteDocument created = noteService.create(request.ownerId(), request.title(), request.content(), request.tags());
        return ResponseEntity.created(URI.create("/api/notes/" + created.id())).body(NoteResponse.from(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteResponse> update(@PathVariable UUID id, @Valid @RequestBody NoteRequest request, Authentication authentication) {
        return ResponseEntity.ok(NoteResponse.from(
                noteService.update(id, authentication.getName(), request.title(), request.content(), request.tags())
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication authentication) {
        noteService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
