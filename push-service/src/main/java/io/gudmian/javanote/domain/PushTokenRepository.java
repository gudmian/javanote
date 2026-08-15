package io.gudmian.javanote.domain;

import io.gudmian.javanote.data.PushTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * @author d.guba
 */
public interface PushTokenRepository extends JpaRepository<PushTokenEntity, UUID> {
    List<PushTokenEntity> findAllByOwnerId(UUID ownerId);

    boolean existsByOwnerIdAndToken(UUID ownerId, String token);

    void deleteByOwnerIdAndToken(UUID ownerId, String token);
}
