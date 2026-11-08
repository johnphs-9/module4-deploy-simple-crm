package sg.edu.ntu.simple_crm_api.auth;

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
 * Credentials are validated with the simple fixed-check mechanism required by
 * the
 * assignment (assignment.md L25). The literal values are externalised to
 * configuration
 * (application-dev/​-test.properties), sourced from the gitignored .env
 * {@code APP_USER_USERNAME} / {@code APP_USER_PASSWORD} (assignment defaults:
 * user/password).
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;
    private final String validUsername;
    private final String validPassword;

    public AuthController(JwtService jwtService,
            @Value("${app.auth.username}") String validUsername,
            @Value("${app.auth.password}") String validPassword) {
        this.jwtService = jwtService;
        this.validUsername = validUsername;
        this.validPassword = validPassword;
    }

    /**
     * POST /auth/login — issues a JWT for valid credentials.
     *
     * @return 200 with a TokenResponse for valid credentials; 401 otherwise
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        if (request != null
                && validUsername.equals(request.getUsername())
                && validPassword.equals(request.getPassword())) {
            String token = jwtService.generateToken(request.getUsername());
            return ResponseEntity.ok(new TokenResponse(token));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}