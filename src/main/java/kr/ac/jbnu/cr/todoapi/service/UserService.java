package kr.ac.jbnu.cr.todoapi.service;

import kr.ac.jbnu.cr.todoapi.dto.request.RegisterRequest;
import kr.ac.jbnu.cr.todoapi.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {

    private final Map<Long, User> userStorage = new HashMap<>();
    private final Map<String, User> usernameIndex = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final PasswordEncoder passwordEncoder;

    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest request) {
        User user = User.builder()
                .id(idGenerator.getAndIncrement())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .build();

        userStorage.put(user.getId(), user);
        usernameIndex.put(user.getUsername(), user);

        return user;
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(usernameIndex.get(username));
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(userStorage.get(id));
    }

    public boolean existsByUsername(String username) {
        return usernameIndex.containsKey(username);
    }

    /**
     * Verify password (comme dans le cours)
     */
    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }
}