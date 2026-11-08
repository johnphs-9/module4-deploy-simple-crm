package sg.edu.ntu.simple_crm_api.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for POST /auth/login: the issued JWT (Assignment PT2 §1).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {
    private String token;
}