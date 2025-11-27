package kr.ac.jbnu.cr.todoapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import kr.ac.jbnu.cr.todoapi.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expiration;
    private final String issuer;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration,
            @Value("${jwt.issuer}") String issuer) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
        this.issuer = issuer;
    }

    /**
     * Token generation logic (comme dans le cours)
     */
    public String createToken(User user) {
        return Jwts.builder()
                .signWith(key)
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .claim("username", user.getUsername())
                .compact();
    }

    /**
     * Validate token and get user ID
     */
    public Optional<Long> getUser(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return Optional.of(Long.parseLong(claims.getSubject()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Validate token
     */
    public boolean validateToken(String token) {
        return getUser(token).isPresent();
    }
}