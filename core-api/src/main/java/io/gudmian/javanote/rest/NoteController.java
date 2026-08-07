package io.gudmian.javanote.rest;

import io.gudmian.javanote.domain.Note;
import io.gudmian.javanote.domain.NoteRepository;
import io.gudmian.javanote.dto.NoteRequest;
import io.gudmian.javanote.dto.NoteResponse;
import io.gudmian.javanote.utils.NoteNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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

    private final NoteRepository noteRepository;

    public NoteController(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @GetMapping
    public List<NoteResponse> readAll() {
        return noteRepository.readAll().stream()
                .map(NoteResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteResponse> read(@PathVariable UUID id) {
        return noteRepository.read(id)
                .map(NoteResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new NoteNotFoundException(id));
    }

    @PostMapping
    public ResponseEntity<NoteResponse> create(@Valid @RequestBody NoteRequest request) {
        Note created = noteRepository.create(request.title(), request.content(), request.tags());
        NoteResponse response = NoteResponse.from(created);
        return ResponseEntity.created(URI.create("/api/notes/" + created.id())).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody NoteRequest request
    ) {
        return noteRepository.update(id, request.title(), request.content(), request.tags())
                .map(NoteResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new NoteNotFoundException(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        boolean deleted = noteRepository.delete(id);

        if (!deleted) {
            throw new NoteNotFoundException(id);
        }

        return ResponseEntity.noContent().build();
    }
}
