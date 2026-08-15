package io.gudmian.javanote.web.auth;

import io.gudmian.javanote.data.user.UserEntity;
import io.gudmian.javanote.domain.user.UserRepository;
import io.gudmian.javanote.dto.auth.RegisterForm;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;

/**
 * @author d.guba
 */
@Controller
@RequestMapping
public class AuthViewController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthViewController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterForm form, BindingResult bindingResult) {
        if (userRepository.findByUsername(form.getUsername()).isPresent()) {
            bindingResult.rejectValue("username", "duplicate", "Такой логин уже занят");
        }
        if (bindingResult.hasErrors()) {
            return "register";
        }
        UserEntity entity = new UserEntity();
        entity.setUsername(form.getUsername());
        entity.setPassword(passwordEncoder.encode(form.getPassword()));
        entity.setCreatedAt(Instant.now());
        userRepository.save(entity);
        return "redirect:/login";
    }
}
