package io.gudmian.javanote.consumer;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import io.gudmian.javanote.event.notes.NoteCreatedEvent;
import io.gudmian.javanote.event.notes.NoteEventsTopics;
import io.gudmian.javanote.service.DeviceTokenStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * @author d.guba
 */
@Component
public class NoteEventListener {
    private static final Logger log = LoggerFactory.getLogger(NoteEventListener.class);

    private final DeviceTokenStore deviceTokenStore;
    private final FirebaseMessaging firebaseMessaging;

    public NoteEventListener(
            DeviceTokenStore deviceTokenStore,
            FirebaseMessaging firebaseMessaging
    ) {
        this.deviceTokenStore = deviceTokenStore;
        this.firebaseMessaging = firebaseMessaging;
    }

    @KafkaListener(topics = NoteEventsTopics.NOTE_CREATED)
    public void onNoteCreated(NoteCreatedEvent event) throws FirebaseMessagingException {
        List<String> tokens = deviceTokenStore.tokensFor(event.ownerId());
        if (tokens.isEmpty()) {
            log.info("No device tokens registered for owner {}", event.ownerId());
            return;
        }

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder()
                        .setTitle("Новая заметка")
                        .setBody(event.title())
                        .build())
                .build();

        BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
        log.info("Push sent for owner {}: {} success, {} failure",
                event.ownerId(), response.getSuccessCount(), response.getFailureCount());
        cleanupUnregisteredTokens(event.ownerId(), tokens, response);
    }

    private void cleanupUnregisteredTokens(UUID ownerId, List<String> tokens, BatchResponse response) {
        List<SendResponse> results = response.getResponses();
        for (int i = 0; i < tokens.size(); i++) {
            SendResponse result = results.get(i);
            if (result.isSuccessful()) {
                continue;
            }
            MessagingErrorCode errorCode = result.getException().getMessagingErrorCode();
            if (errorCode == MessagingErrorCode.UNREGISTERED || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                String staleToken = tokens.get(i);
                log.info("Removing unregistered token for owner {}: {}", ownerId, staleToken);
                deviceTokenStore.unregister(ownerId, staleToken);
            }
        }
    }
}
