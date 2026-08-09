package io.gudmian.javanote.dto;

import io.gudmian.javanote.data.UserEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * @author d.guba
 */
public record UserResponse(
        UUID id,
        String username,
        Instant createdAt
) {
    public static UserResponse from(UserEntity entity) {
        return new UserResponse(
                entity.getId(),
                entity.getUsername(),
                entity.getCreatedAt()
        );
    }
}
