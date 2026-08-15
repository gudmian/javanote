package io.gudmian.javanote;

import io.gudmian.javanote.data.notes.NoteDocument;
import io.gudmian.javanote.dto.notes.NoteRequest;
import io.gudmian.javanote.rest.notes.NoteController;
import io.gudmian.javanote.service.notes.NoteService;
import io.gudmian.javanote.utils.notes.NoteAccessDeniedException;
import io.gudmian.javanote.utils.notes.NoteNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
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
    private NoteService noteService;

    @MockitoBean
    private CacheManager cacheManager;

    private static Authentication authenticationOf(String username) {
        return new UsernamePasswordAuthenticationToken(username, null, List.of());
    }

    private static NoteDocument noteOf(UUID id, UUID ownerId, String title, String content) {
        Instant now = Instant.now();
        return new NoteDocument(id, ownerId, title, content, List.of(), now, now);
    }

    @Test
    void readAll_returnOwnNotes() {
        UUID ownerId = UUID.randomUUID();
        given(noteService.findAllForOwner("alice")).willReturn(
                List.of(
                        noteOf(UUID.randomUUID(), ownerId, "First", "Content"),
                        noteOf(UUID.randomUUID(), ownerId, "Second", "Content")
                )
        );

        assertThat(mvc.get().uri("/api/notes").principal(authenticationOf("alice")))
                .hasStatusOk().bodyJson();
    }

    @Test
    void read_returnNote() {
        UUID uuid = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        given(noteService.findByIdForOwner(uuid, "alice"))
                .willReturn(noteOf(uuid, ownerId, "First", "Content"));

        assertThat(mvc.get().uri("/api/notes/" + uuid).principal(authenticationOf("alice")))
                .hasStatusOk().bodyJson();
    }

    @Test
    void read_notFound() {
        UUID uuid = UUID.randomUUID();

        given(noteService.findByIdForOwner(uuid, "alice"))
                .willThrow(new NoteNotFoundException(uuid));

        assertThat(mvc.get().uri("/api/notes/" + uuid).principal(authenticationOf("alice")))
                .hasStatus(HttpStatus.NOT_FOUND).bodyJson();
    }

    @Test
    void read_accessDenied() {
        UUID uuid = UUID.randomUUID();

        given(noteService.findByIdForOwner(uuid, "mallory"))
                .willThrow(new NoteAccessDeniedException(uuid));

        assertThat(mvc.get().uri("/api/notes/" + uuid).principal(authenticationOf("mallory")))
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    void create_success() {
        UUID ownerId = UUID.randomUUID();
        NoteRequest request = new NoteRequest(ownerId, "Title", "Content", List.of());

        given(noteService.create(ownerId, "Title", "Content", List.of()))
                .willReturn(noteOf(UUID.randomUUID(), ownerId, "Title", "Content"));

        assertThat(mvc.post().uri("/api/notes").principal(authenticationOf("alice"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .hasStatus(HttpStatus.CREATED)
                .bodyJson();
    }

    @Test
    void create_notValid() {
        UUID ownerId = UUID.randomUUID();
        NoteRequest request = new NoteRequest(ownerId, "", "Content", List.of());

        assertThat(mvc.post().uri("/api/notes").principal(authenticationOf("alice"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void update_success() {
        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        NoteRequest request = new NoteRequest(ownerId, "Title", "Content", List.of());

        given(noteService.update(id, "alice", "Title", "Content", List.of()))
                .willReturn(noteOf(id, ownerId, "Title", "Content"));

        assertThat(mvc.put().uri("/api/notes/" + id).principal(authenticationOf("alice"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .hasStatusOk()
                .bodyJson();
    }

    @Test
    void update_notFound() {
        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        NoteRequest request = new NoteRequest(ownerId, "Title", "Content", List.of());

        given(noteService.update(id, "alice", "Title", "Content", List.of()))
                .willThrow(new NoteNotFoundException(id));

        assertThat(mvc.put().uri("/api/notes/" + id).principal(authenticationOf("alice"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void update_accessDenied() {
        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        NoteRequest request = new NoteRequest(ownerId, "Title", "Content", List.of());

        given(noteService.update(id, "mallory", "Title", "Content", List.of()))
                .willThrow(new NoteAccessDeniedException(id));

        assertThat(mvc.put().uri("/api/notes/" + id).principal(authenticationOf("mallory"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    void delete_success() {
        UUID uuid = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        given(noteService.delete(uuid, "alice"))
                .willReturn(noteOf(uuid, ownerId, "First", "Content"));

        assertThat(mvc.delete().uri("/api/notes/" + uuid).principal(authenticationOf("alice")))
                .hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    void delete_notFound() {
        UUID uuid = UUID.randomUUID();

        given(noteService.delete(uuid, "alice"))
                .willThrow(new NoteNotFoundException(uuid));

        assertThat(mvc.delete().uri("/api/notes/" + uuid).principal(authenticationOf("alice")))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void delete_accessDenied() {
        UUID uuid = UUID.randomUUID();

        given(noteService.delete(uuid, "mallory"))
                .willThrow(new NoteAccessDeniedException(uuid));

        assertThat(mvc.delete().uri("/api/notes/" + uuid).principal(authenticationOf("mallory")))
                .hasStatus(HttpStatus.FORBIDDEN);
    }
}
