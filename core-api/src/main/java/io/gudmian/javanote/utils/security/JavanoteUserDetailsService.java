package io.gudmian.javanote.utils.security;

import io.gudmian.javanote.data.UserEntity;
import io.gudmian.javanote.domain.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author d.guba
 */
@Service
public class JavanoteUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public JavanoteUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return User.builder()
                .username(userEntity.getUsername())
                .password(userEntity.getPassword())
                .roles("USER")
                .build();
    }
}
