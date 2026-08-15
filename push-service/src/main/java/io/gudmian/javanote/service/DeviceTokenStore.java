package io.gudmian.javanote.service;

import io.gudmian.javanote.data.PushTokenEntity;
import io.gudmian.javanote.domain.PushTokenRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * @author d.guba
 */
@Service
public class DeviceTokenStore {

    private final PushTokenRepository pushTokenRepository;

    public DeviceTokenStore(PushTokenRepository pushTokenRepository) {
        this.pushTokenRepository = pushTokenRepository;
    }

    public void register(UUID ownerId, String token) {
        if (pushTokenRepository.existsByOwnerIdAndToken(ownerId, token)) {
            return;
        }

        PushTokenEntity entity = new PushTokenEntity();
        entity.setOwnerId(ownerId);
        entity.setToken(token);
        entity.setCreatedAt(Instant.now());
        pushTokenRepository.save(entity);
    }

    public void unregister(UUID ownerId, String token) {
        pushTokenRepository.deleteByOwnerIdAndToken(ownerId, token);
    }

    public List<String> tokensFor(UUID ownerId) {
        return pushTokenRepository.findAllByOwnerId(ownerId)
                .stream()
                .map(PushTokenEntity::getToken)
                .toList();
    }
}
