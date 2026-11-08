package sg.edu.ntu.simple_crm_api.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc; // D8 — Boot 4 package
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import sg.edu.ntu.simple_crm_api.security.JwtService;

/**
 * End-to-end JWT enforcement on the protected endpoint (J11/J12/J13) —
 * mirrors the assignment's full Postman flow (J14): no token → 401,
 * login token → 200, bad/expired token → 401. Runs the real security
 * filter chain (including JwtAuthFilter) via MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class HelloApiSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Test
    void unauthenticatedRequest_returns401WithBasicChallenge() throws Exception {
        // Regression for browser access: the 401 must carry a
        // WWW-Authenticate: Basic response header so Chrome's native login
        // dialog appears on a plain URL (URL-embedded creds are ignored since
        // Chrome 59).
        mockMvc.perform(get("/customers"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", containsString("Basic")));
    }

    @Test
    void apiHello_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void apiHello_withValidToken_returnsHello() throws Exception {
        String token = jwtService.generateToken("user");
        mockMvc.perform(get("/api/hello")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello! JWT authentication successful."));
    }

    @Test
    void apiHello_withGarbageToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/hello")
                .header("Authorization", "Bearer garbage.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void apiHello_withExpiredToken_returnsUnauthorized() throws Exception {
        // Build a correctly-formed but expired token with the app's own secret.
        String secret = (String) ReflectionTestUtils.getField(jwtService, "jwtSecret");
        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        String expired = Jwts.builder().setSubject("user")
                .setIssuedAt(new Date(now.getTime() - 7_200_000))
                .setExpiration(new Date(now.getTime() - 3_600_000))
                .signWith(key, SignatureAlgorithm.HS256).compact();
        mockMvc.perform(get("/api/hello")
                .header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void apiHello_withValidToken_roleMappedFromUserStore() throws Exception {
        // Regression: token for "user" maps to ROLE_USER via UserDetailsService (D2),
        // so pt1 rules still apply: GET /customers 200, POST /customers 403.
        String token = jwtService.generateToken("user");
        mockMvc.perform(get("/customers").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void errorPage_isPublic_soDeniedBearerRequestsKeepTheir403() throws Exception {
        // Tomcat re-runs the filter chain on the ERROR dispatch to /error where
        // the once-per-request Bearer filter no longer applies; if /error were
        // secured, the entry point would rewrite a denied (403) Bearer response
        // to 401. Boot's error page must therefore stay public (not 401).
        int status = mockMvc.perform(get("/error")).andReturn().getResponse().getStatus();
        org.junit.jupiter.api.Assertions.assertNotEquals(401, status,
                "/error was rewritten to 401 — check the /error permitAll rule in SecurityConfig");
    }

    @Test
    void apiBearer_adminToken_grantsAdminRights() throws Exception {
        // Login now issues tokens for every configured account; JwtAuthFilter maps
        // "admin" to ROLE_ADMIN, so a Bearer token can perform admin-only writes.
        String token = jwtService.generateToken("admin");
        mockMvc.perform(post("/customers")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("{\"firstName\":\"Diana\",\"lastName\":\"Prince\",\"email\":\"diana@prince.com\","
                        + "\"contactNo\":\"87654321\",\"jobTitle\":\"Archivist\",\"yearOfBirth\":1985}"))
                .andExpect(status().isCreated());
    }
}
