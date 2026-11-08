package sg.edu.ntu.simple_crm_api.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/** Unit tests for {@link JwtService} (J6-J8). No Spring context needed. */
class JwtServiceTest {

    /** 48 chars >= 32 bytes, satisfies HS256 key strength. */
    private static final String SECRET = "unit-test-secret-key-0123456789-0123456789";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 3_600_000L);
    }

    @Test
    void generateToken_setsSubjectIatAndFutureExpiry() {
        String token = jwtService.generateToken("user");
        Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        var body = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        assertEquals("user", body.getSubject());
        assertNotNull(body.getIssuedAt());
        assertTrue(body.getExpiration().after(new Date()));
    }

    @Test
    void extractUsername_roundTrips() {
        String token = jwtService.generateToken("user");
        assertEquals("user", jwtService.extractUsername(token));
    }

    @Test
    void isTokenValid_trueForOwnUnexpiredToken() {
        assertTrue(jwtService.isTokenValid(jwtService.generateToken("user")));
    }

    @Test
    void isTokenValid_falseForGarbage() {
        assertFalse(jwtService.isTokenValid("not.a.jwt"));
        assertFalse(jwtService.isTokenValid(""));
    }

    @Test
    void isTokenValid_falseForTamperedToken() {
        String token = jwtService.generateToken("user");
        String tampered = token.substring(0, token.length() - 5) + "AAAAA"; // corrupt signature
        assertFalse(jwtService.isTokenValid(tampered));
    }

    @Test
    void isTokenValid_falseForTokenSignedWithDifferentKey() {
        Key otherKey = Keys.hmacShaKeyFor("other-secret-key-123456789-123456789".getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        String foreign = Jwts.builder().setSubject("user")
                .setIssuedAt(now).setExpiration(new Date(now.getTime() + 60_000))
                .signWith(otherKey, SignatureAlgorithm.HS256).compact();
        assertFalse(jwtService.isTokenValid(foreign));
    }

    @Test
    void isTokenValid_falseForExpiredToken() {
        Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        String expired = Jwts.builder().setSubject("user")
                .setIssuedAt(new Date(now.getTime() - 7_200_000))
                .setExpiration(new Date(now.getTime() - 3_600_000))
                .signWith(key, SignatureAlgorithm.HS256).compact();
        assertFalse(jwtService.isTokenValid(expired)); // expired, otherwise valid
    }
}
