package io.gudmian.javanote.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author d.guba
 */
@NoArgsConstructor
@Getter
@Setter
public class RegisterForm {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
