package io.gudmian.javanote;

import io.gudmian.javanote.data.NoteDocument;
import io.gudmian.javanote.domain.NoteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author d.guba
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
public class NotesRepositoryIT {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

    @Autowired
    NoteRepository noteRepository;

    @Test
    void noteSaveAndFindById() {
        UUID id = UUID.randomUUID();
        NoteDocument document = new NoteDocument(
                id,
                UUID.randomUUID(),
                "Title",
                "Contnet",
                List.of(),
                Instant.now(),
                Instant.now()
        );
        NoteDocument saved = noteRepository.save(document);

        assertThat(noteRepository.findById(saved.id())).isPresent();
    }
}
