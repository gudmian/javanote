package io.gudmian.javanote;

import io.gudmian.javanote.data.NoteDocument;
import io.gudmian.javanote.data.UserEntity;
import io.gudmian.javanote.domain.NoteRepository;
import io.gudmian.javanote.domain.UserRepository;
import io.gudmian.javanote.dto.NoteRequest;
import io.gudmian.javanote.rest.NoteController;
import io.gudmian.javanote.utils.NoteNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

    @MockitoBean
    private UserRepository userRepository;

    private static UserEntity userWithId(UUID id, String username) {
        UserEntity entity = new UserEntity();
        entity.setId(id);
        entity.setUsername(username);
        return entity;
    }

    private static Authentication authenticationOf(String username) {
        return new UsernamePasswordAuthenticationToken(username, null, List.of());
    }

    @Test
    void readAll_returnOwnNotes() {
        UUID ownerId = UUID.randomUUID();
        given(userRepository.findByUsername("alice")).willReturn(Optional.of(userWithId(ownerId, "alice")));
        given(repository.findAllByOwnerId(ownerId)).willReturn(
                List.of(
                        new NoteDocument(
                                UUID.randomUUID(),
                                ownerId,
                                "First",
                                "Content",
                                List.of(),
                                Instant.now(),
                                Instant.now()
                        ),
                        new NoteDocument(
                                UUID.randomUUID(),
                                ownerId,
                                "Second",
                                "Content",
                                List.of(),
                                Instant.now(),
                                Instant.now()
                        )
                )
        );

        assertThat(mvc.get().uri("/api/notes").principal(authenticationOf("alice")))
                .hasStatusOk().bodyJson();
    }

    @Test
    void read_returnNote() {
        UUID uuid = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        given(userRepository.findByUsername("alice")).willReturn(Optional.of(userWithId(ownerId, "alice")));
        given(repository.findById(uuid)).willReturn(
                Optional.of(
                        new NoteDocument(
                                uuid,
                                ownerId,
                                "First",
                                "Content",
                                List.of(),
                                Instant.now(),
                                Instant.now()
                        )
                )
        );

        assertThat(mvc.get().uri("/api/notes/" + uuid).principal(authenticationOf("alice")))
                .hasStatusOk().bodyJson();
    }

    @Test
    void read_notFound() {
        UUID uuid = UUID.randomUUID();
        UUID anotherUuid = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        given(repository.findById(uuid)).willReturn(
                Optional.of(
                        new NoteDocument(
                                uuid,
                                ownerId,
                                "First",
                                "Content",
                                List.of(),
                                Instant.now(),
                                Instant.now()
                        )
                )
        );

        assertThat(mvc.get().uri("/api/notes/" + anotherUuid).principal(authenticationOf("alice")))
                .hasStatus(HttpStatus.NOT_FOUND).bodyJson();
    }

    @Test
    void read_accessDenied() {
        UUID uuid = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        given(userRepository.findByUsername("mallory")).willReturn(Optional.of(userWithId(otherUserId, "mallory")));
        given(repository.findById(uuid)).willReturn(
                Optional.of(
                        new NoteDocument(
                                uuid,
                                ownerId,
                                "First",
                                "Content",
                                List.of(),
                                Instant.now(),
                                Instant.now()
                        )
                )
        );

        assertThat(mvc.get().uri("/api/notes/" + uuid).principal(authenticationOf("mallory")))
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    void create_success() {
        UUID ownerId = UUID.randomUUID();
        NoteRequest request = new NoteRequest(ownerId, "Title", "Content", List.of());

        given(repository.save(any(NoteDocument.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

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
        Instant now = Instant.now();
        NoteRequest request = new NoteRequest(ownerId, "Title", "Content", List.of());
        NoteDocument oldNote = new NoteDocument(id, ownerId, "Old", "Old", request.tags(), now, now);

        given(userRepository.findByUsername("alice")).willReturn(Optional.of(userWithId(ownerId, "alice")));
        given(repository.findById(id)).willReturn(Optional.of(oldNote));
        given(repository.save(any(NoteDocument.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

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
        given(repository.findById(id)).willThrow(new NoteNotFoundException(id));

        assertThat(mvc.put().uri("/api/notes/" + id).principal(authenticationOf("alice"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void update_accessDenied() {
        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        Instant now = Instant.now();
        NoteRequest request = new NoteRequest(ownerId, "Title", "Content", List.of());
        NoteDocument existing = new NoteDocument(id, ownerId, "Old", "Old", request.tags(), now, now);

        given(userRepository.findByUsername("mallory")).willReturn(Optional.of(userWithId(otherUserId, "mallory")));
        given(repository.findById(id)).willReturn(Optional.of(existing));

        assertThat(mvc.put().uri("/api/notes/" + id).principal(authenticationOf("mallory"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    void delete_success() {
        UUID uuid = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        given(userRepository.findByUsername("alice")).willReturn(Optional.of(userWithId(ownerId, "alice")));
        given(repository.findById(uuid)).willReturn(
                Optional.of(
                        new NoteDocument(
                                uuid,
                                ownerId,
                                "First",
                                "Content",
                                List.of(),
                                Instant.now(),
                                Instant.now()
                        )
                )
        );

        assertThat(mvc.delete().uri("/api/notes/" + uuid).principal(authenticationOf("alice")))
                .hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    void delete_notFound() {
        UUID uuid = UUID.randomUUID();
        given(repository.findById(uuid)).willReturn(Optional.empty());

        assertThat(mvc.delete().uri("/api/notes/" + uuid).principal(authenticationOf("alice")))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void delete_accessDenied() {
        UUID uuid = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        given(userRepository.findByUsername("mallory")).willReturn(Optional.of(userWithId(otherUserId, "mallory")));
        given(repository.findById(uuid)).willReturn(
                Optional.of(
                        new NoteDocument(
                                uuid,
                                ownerId,
                                "First",
                                "Content",
                                List.of(),
                                Instant.now(),
                                Instant.now()
                        )
                )
        );

        assertThat(mvc.delete().uri("/api/notes/" + uuid).principal(authenticationOf("mallory")))
                .hasStatus(HttpStatus.FORBIDDEN);
    }
}
