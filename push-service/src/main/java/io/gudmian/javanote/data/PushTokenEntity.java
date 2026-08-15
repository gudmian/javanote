package io.gudmian.javanote.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * @author d.guba
 */
@Entity
@Table(name = "push_tokens", uniqueConstraints = @UniqueConstraint(columnNames = {"owner_id", "token"}))
@NoArgsConstructor
@Getter
@Setter
public class PushTokenEntity {
    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(nullable = false)
    private String token;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
