package sg.edu.ntu.simple_crm_api.auth;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sg.edu.ntu.simple_crm_api.security.JwtService;

/**
 * JWT issuance endpoint (Assignment PT2 §1).
 * <p>
 * Credentials are validated with the simple fixed-check mechanism required by
 * the assignment (assignment.md L25), extended to the configured in-memory
 * accounts (user / manager / admin — {@code app.auth.*} in
 * application-dev.properties, sourced from the gitignored {@code .env}).
 * <p>
 * Each issued token carries the account name as its subject;
 * {@code JwtAuthFilter} maps the subject to the pt1 RBAC roles
 * (ROLE_USER / ROLE_MANAGER / ROLE_ADMIN) via {@code UserDetailsService}.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;
    private final Map<String, String> validAccounts;

    public AuthController(JwtService jwtService,
            @Value("${app.auth.username}") String username,
            @Value("${app.auth.password}") String password,
            @Value("${app.auth.manager.username}") String managerUsername,
            @Value("${app.auth.manager.password}") String managerPassword,
            @Value("${app.auth.admin.username}") String adminUsername,
            @Value("${app.auth.admin.password}") String adminPassword) {
        this.jwtService = jwtService;

        Map<String, String> accounts = new HashMap<>();
        accounts.put(username, password);
        accounts.put(managerUsername, managerPassword);
        accounts.put(adminUsername, adminPassword);
        this.validAccounts = accounts;
    }

    /**
     * POST /auth/login — issues a JWT for any configured account (user,
     * manager or admin) with valid credentials.
     *
     * @return 200 with a TokenResponse for valid credentials; 401 otherwise
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        if (request == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String expectedPassword = validAccounts.get(request.getUsername());
        if (expectedPassword != null && expectedPassword.equals(request.getPassword())) {
            String token = jwtService.generateToken(request.getUsername());
            return ResponseEntity.ok(new TokenResponse(token));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
