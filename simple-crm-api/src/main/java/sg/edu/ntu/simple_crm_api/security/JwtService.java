package sg.edu.ntu.simple_crm_api.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * Issues and validates HS256-signed JWTs (Assignment PT2 §1).
 * Configuration: {@code jwt.secret} and {@code jwt.expiration-ms} from the
 * profile property files
 * (application-dev/application-test.properties); the secret value comes from
 * the gitignored .env.
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    /** Derives the HMAC-SHA key from the configured secret. */
    private Key signingKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * J6 — generates a token with the given username (subject), iat and future exp.
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** J7 — extracts the username (subject) from the token. */
    public String extractUsername(String token) {
        return parse(token).getSubject();
    }

    /**
     * J8 — validates signature and expiry; true only for well-formed, unexpired,
     * correctly-signed tokens.
     */
    public boolean isTokenValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}