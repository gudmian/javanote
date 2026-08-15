package io.gudmian.javanote;

import io.gudmian.javanote.data.user.UserEntity;
import io.gudmian.javanote.domain.user.UserRepository;
import io.gudmian.javanote.event.notes.NoteCreatedEvent;
import io.gudmian.javanote.event.notes.NoteEventsTopics;
import io.gudmian.javanote.service.notes.NoteService;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author d.guba
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
public class NoteEventProducerIT {

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:latest"));

    @Autowired
    private NoteService noteService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void publishesEventAfterNoteCreated() {
        UserEntity user = new UserEntity();
        user.setUsername("kafka-test-user");
        user.setPassword("irrelevant");
        user.setCreatedAt(Instant.now());
        UUID ownerId = userRepository.save(user).getId();

        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "note-events-test");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (Consumer<String, NoteCreatedEvent> consumer = new DefaultKafkaConsumerFactory<>(
                consumerProps, new StringDeserializer(), new JacksonJsonDeserializer<>(NoteCreatedEvent.class, false))
                .createConsumer()) {

            consumer.subscribe(List.of(NoteEventsTopics.NOTE_CREATED));

            noteService.create(ownerId, "Тест", "Текст", List.of());

            ConsumerRecords<String, NoteCreatedEvent> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10));

            assertThat(records.count()).isEqualTo(1);
            NoteCreatedEvent event = records.iterator().next().value();
            assertThat(event.ownerId()).isEqualTo(ownerId);
            assertThat(event.title()).isEqualTo("Тест");
        }
    }
}