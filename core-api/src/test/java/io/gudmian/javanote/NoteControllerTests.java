package io.gudmian.javanote;

import io.gudmian.javanote.domain.Note;
import io.gudmian.javanote.domain.NoteRepository;
import io.gudmian.javanote.dto.NoteRequest;
import io.gudmian.javanote.rest.NoteController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * @author d.guba
 */
@WebMvcTest(NoteController.class)
public class NoteControllerTests {

    @Autowired
    private MockMvcTester mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NoteRepository repository;

    @Test
    void readAll_returnNotes() {
        given(repository.readAll()).willReturn(
                List.of(
                        new Note(
                                UUID.randomUUID(),
                                "First",
                                "Content",
                                List.of(),
                                Instant.now(),
                                Instant.now()
                        ),
                        new Note(
                                UUID.randomUUID(),
                                "First",
                                "Content",
                                List.of(),
                                Instant.now(),
                                Instant.now()
                        )
                )
        );

        assertThat(mvc.get().uri("/api/notes"))
                .hasStatusOk().bodyJson();
    }

    @Test
    void read_returnNote() {
        UUID uuid = UUID.randomUUID();

        given(repository.read(uuid)).willReturn(
                Optional.of(
                        new Note(
                                uuid,
                                "First",
                                "Content",
                                List.of(),
                                Instant.now(),
                                Instant.now()
                        )
                )
        );

        assertThat(mvc.get().uri("/api/notes/" + uuid))
                .hasStatusOk().bodyJson();
    }

    @Test
    void read_notFound() {
        UUID uuid = UUID.randomUUID();
        UUID anotherUuid = UUID.randomUUID();

        given(repository.read(uuid)).willReturn(
                Optional.of(
                        new Note(
                                uuid,
                                "First",
                                "Content",
                                List.of(),
                                Instant.now(),
                                Instant.now()
                        )
                )
        );

        assertThat(mvc.get().uri("/api/notes/" + anotherUuid))
                .hasStatus(HttpStatus.NOT_FOUND).bodyJson();
    }

    @Test
    void create_success() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        NoteRequest request = new NoteRequest("Title", "Content", List.of());
        Note note = new Note(id, request.title(), request.content(), request.tags(), now, now);

        given(repository.create(note.title(), note.content(), note.tags()))
                .willReturn(note);

        assertThat(mvc.post().uri("/api/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .hasStatus(HttpStatus.CREATED)
                .bodyJson();
    }

    @Test
    void create_notValid() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        NoteRequest request = new NoteRequest("", "Content", List.of());
        Note note = new Note(id, request.title(), request.content(), request.tags(), now, now);

        given(repository.create(request.title(), request.content(), request.tags()))
                .willReturn(note);

        assertThat(mvc.post().uri("/api/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void update_success() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        NoteRequest request = new NoteRequest("Title", "Content", List.of());
        Note note = new Note(id, request.title(), request.content(), request.tags(), now, now);

        given(repository.update(id, request.title(), request.content(), request.tags()))
                .willReturn(Optional.of(note));

        assertThat(mvc.put().uri("/api/notes/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .hasStatusOk()
                .bodyJson();
    }

    @Test
    void update_notFound() {
        UUID id = UUID.randomUUID();
        NoteRequest request = new NoteRequest("Title", "Content", List.of());
        given(repository.update(id, request.title(), request.content(), request.tags()))
                .willReturn(Optional.empty());
        assertThat(mvc.put().uri("/api/notes/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void delete_success() {
        UUID uuid = UUID.randomUUID();

        given(repository.delete(uuid)).willReturn(true);

        assertThat(mvc.delete().uri("/api/notes/" + uuid))
                .hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    void delete_notFound() {
        UUID uuid = UUID.randomUUID();

        given(repository.delete(uuid)).willReturn(false);

        assertThat(mvc.delete().uri("/api/notes/" + uuid))
                .hasStatus(HttpStatus.NOT_FOUND);
    }
}
