package io.gudmian.javanote.rest.user;

import io.gudmian.javanote.data.user.UserEntity;
import io.gudmian.javanote.domain.user.UserRepository;
import io.gudmian.javanote.dto.user.UserRequest;
import io.gudmian.javanote.dto.user.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Instant;

/**
 * @author d.guba
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        UserEntity entity = new UserEntity();
        entity.setUsername(request.username());
        entity.setPassword(passwordEncoder.encode(request.password()));
        entity.setCreatedAt(Instant.now());
        UserEntity created = userRepository.save(entity);
        return ResponseEntity.created(URI.create("/api/users/" + created.getId()))
                .body(UserResponse.from(created));
    }

}
