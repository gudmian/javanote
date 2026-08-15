package io.gudmian.javanote;

import io.gudmian.javanote.data.user.UserEntity;
import io.gudmian.javanote.domain.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author d.guba
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
public class UserRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgress = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    UserRepository userRepository;

    @Test
    void userCreateAndFindTest() {
        UserEntity entity = new UserEntity();
        entity.setUsername("alice");
        entity.setPassword("secret");
        entity.setCreatedAt(Instant.now());
        UserEntity saved = userRepository.save(entity);
        assertThat(userRepository.findById(saved.getId())).isPresent();
    }

}
