package io.gudmian.javanote.controller;

import io.gudmian.javanote.dto.PushRegisterRequest;
import io.gudmian.javanote.service.DeviceTokenStore;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author d.guba
 */
@RestController
@RequestMapping("/api/push")
public class PushController {

    private final DeviceTokenStore deviceTokenStore;

    public PushController(DeviceTokenStore deviceTokenStore) {
        this.deviceTokenStore = deviceTokenStore;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody PushRegisterRequest request) {
        deviceTokenStore.register(
                request.userId(),
                request.token()
        );

        return ResponseEntity.ok().build();
    }
}
