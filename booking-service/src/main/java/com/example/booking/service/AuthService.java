package com.example.booking.service;

import com.example.booking.entity.User;
import com.example.booking.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private final SecretKey key;

    public AuthService(
            UserRepository userRepository,
            @Value("${security.jwt.secret}") String secret
    ) {
        this.userRepository = userRepository;

        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, bytes.length);
            bytes = padded;
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public User register(String username, String password, boolean admin) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalStateException("Username already exists: " + username);
        }

        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(password));
        u.setRole(admin ? User.Role.ADMIN : User.Role.USER);

        return userRepository.save(u);
    }

    public String login(String username, String password) {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!encoder.matches(password, u.getPasswordHash())) {
            throw new IllegalArgumentException("Bad credentials");
        }

        Instant now = Instant.now();
        return Jwts.builder()
                .subject(u.getId().toString())
                .claims(Map.of(
                        "scope", u.getRole().name(),
                        "username", u.getUsername()
                ))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)))
                .signWith(key)
                .compact();
    }
}