package io.gudmian.javanote.dto.user;

import io.gudmian.javanote.data.user.UserEntity;

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
