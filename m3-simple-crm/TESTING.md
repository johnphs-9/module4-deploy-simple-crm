# m3-simple-crm — PT2 JWT Authentication: Testing & Verification

- **Phase:** PT2 — JWT Authentication (assignment `ai-4.2-secure-software-development-and-authentication-pt2`)
- **Date:** 2026-10-30
- **Restored:** 2026-10-30 21:30 +08 — the file had been deleted from the working tree; restored verbatim from the reviewer's copy, with the config-location sections updated for the 21:15 secret/credential externalisation (see deviation 3 below).
- **Module:** `m3-simple-crm/m3-simple-crm` (Spring Boot 4.1.1, Java 21, Maven wrapper `./mvnw`)
- **Build plan:** `docs/handoffs/m3-simple-crm-pt2-jwt-auth-build-plan-Qwen3.8-27B-MLX-4bit-2026-10-30T13-07.md`
- **Step status:** `docs/handoffs/m3-simple-crm-pt2-jwt-auth-steps7-12-status.md` · **Externalisation handoff:** `docs/handoffs/m3-simple-crm-pt2-secret-externalisation-build-handoff-2026-10-30T21-25.md`

This file records the **documented testing results** required by the assignment (J15) and the
**References / Credits** section (J16). It covers both the automated JUnit/MockMvc suite and the
manual curl/Postman matrix run against the live application.

---

## 1. What was built (PT2 scope)

On top of the existing pt1 app (HTTP Basic + RBAC on `/customers/**`), PT2 adds:

| Piece | File | Requirement |
|-------|------|-------------|
| jjwt 0.11.5 dependencies | `pom.xml` | J2 |
| `jwt.secret` / `jwt.expiration-ms` | `application-dev.properties` / `application-test.properties` (secret value from gitignored `.env`: `APP_JWT_SECRET` / `TEST_JWT_SECRET`) | J3 |
| `LoginRequest` DTO | `auth/LoginRequest.java` | J4 |
| `TokenResponse` DTO | `auth/TokenResponse.java` | J5 |
| `JwtService` (generate / extract / validate) | `security/JwtService.java` | J6–J8 |
| `AuthController` `POST /auth/login` | `auth/AuthController.java` (credentials from `.env` via `app.auth.*` → `APP_USER_*`) | J9 |
| `JwtAuthFilter` (`OncePerRequestFilter`) | `security/JwtAuthFilter.java` | J11 |
| `SecurityConfig` (JWT chain + pt1 kept) | `config/SecurityConfig.java` | J12 |
| `GET /api/hello` protected endpoint | `controller/HelloController.java` | J13 |

Two authentication mechanisms now coexist (by design, decision D3):
- **HTTP Basic**: in-memory users configured by `APP_USER_*`, `APP_MANAGER_*`, and `APP_ADMIN_*`, with RBAC on `/customers/**`.
- **JWT Bearer** (pt2): issued at `POST /auth/login` with the assignment's simple fixed-check mechanism whose values come from `.env`
  (`APP_USER_USERNAME=user` / `APP_USER_PASSWORD=password` — the assignment defaults), enforced on `/api/**`.

---

## 2. Automated tests (JUnit / MockMvc)

Run with:

```bash
cd m3-simple-crm/m3-simple-crm
./mvnw test
```

**Result: 39 tests run, 0 failures, 0 errors, 0 skipped — BUILD SUCCESS**
(first recorded run 2026-10-30 20:01 +08; **re-confirmed after the externalisation refactor: 2026-10-30 21:17 +08**).

| Test class | Tests | Covers |
|------------|-------|--------|
| `M3SimpleCrmApplicationTests` | 1 | context loads (baseline) |
| `service/DemoServiceTest` | 10 | pt1 service logic (baseline) |
| `service/CustomerServiceImplTest` | 3 | pt1 service logic (baseline) |
| `controller/CustomerControllerTest` | 9 | pt1 Basic-auth + RBAC (baseline) |
| `security/JwtServiceTest` | 7 | J6–J8: subject/iat/expiry round-trip, extract, valid→true, garbage→false, tampered→false, wrong-key→false, expired→false |
| `controller/AuthControllerTest` | 4 | J9: valid creds → 200 + token; wrong password → 401; wrong username → 401; missing fields → 401 |
| `controller/HelloApiSecurityTest` | 5 | J11–J14: no token → 401; valid Bearer → 200 + exact body; garbage Bearer → 401; expired Bearer → 401; ROLE_USER mapping regression (GET /customers → 200) |

The `HelloApiSecurityTest` class runs the **real security filter chain** (including `JwtAuthFilter`)
via MockMvc under the `test` profile (H2 in-memory `crm-tests`), so the full Postman flow from the
assignment is exercised end-to-end without a database. Note: the test profile's `jwt.secret`
defaults to a fixed 64-char test value (overridden by `TEST_JWT_SECRET` in `.env` when present),
so the suite never depends on the dev secret.

### Deviations from the plan's literal code (in scope, recorded)

1. `security/JwtAuthFilter.java` — `authoritiesFor` return type changed from
   `List<GrantedAuthority>` to `Collection<? extends GrantedAuthority>` (and a
   `java.util.Collection` import added). The plan's literal code did not compile because
   `UserDetails.getAuthorities()` returns `Collection<? extends GrantedAuthority>`, which does not
   convert to `List<GrantedAuthority>` or `Collection<GrantedAuthority>`. The returned collection is
   passed straight to `UsernamePasswordAuthenticationToken`, so behavior is unchanged.
2. `config/SecurityConfig.java` — the plan's Step 6 rewrite (J12/D3/D5) had to be applied during this
   build: the working tree still held the pt1-only chain, so `JwtAuthFilter` was never registered and
   valid tokens returned 401. The plan's full new content was applied: `/auth/login` permitAll,
   `/api/**` authenticated, pt1 matchers kept, `new JwtAuthFilter(...)` instantiated inline and
   `addFilterBefore(..., UsernamePasswordAuthenticationFilter.class)`, `.httpBasic(...)` retained.
3. **Secret & credential externalisation (user-requested, 2026-10-30 21:15 +08)** — `jwt.secret`,
   `jwt.expiration-ms` and the `POST /auth/login` credentials moved out of `application.properties`:
   - `application.properties` now only carries `spring.config.import=optional:file:.env[.properties]`
     (loads the gitignored module-root `.env` for all profiles).
   - Dev: `jwt.secret=${APP_JWT_SECRET}`, `app.auth.username/password=${APP_USER_*}` in
     `application-dev.properties`. Test: same keys with deterministic inline defaults (overridden by
     `.env` values when present) in `application-test.properties`.
   - `AuthController` ctor-injects `@Value("${app.auth.username}")` / `@Value("${app.auth.password}")`
     (the fixed-check mechanism itself is unchanged). `JwtService` is untouched code-wise.
   - Verified: full suite 39/39 at 21:17 **and** a live dev-profile smoke test (login from `.env`
     values → 200 + token; Bearer → 200 + exact body; no token → 401; wrong password → 401).
   - Grading note: assignment §1 L13–15 says to put `jwt.*` in `application.properties`; this is an
     intentional deviation approved by the user, resolving the committed-secret concern (former R4).

---

## 3. Manual verification matrix (J10 + J14) — live app

The matrix was run against the **running application** (dev profile, local Postgres
`simple_crm` on `localhost:5432`, which was up). All results below are actual captured responses
(captured 20:0x +08 with the pre-externalisation configuration; re-verified in reduced form at
21:17 after the refactor — login 200 + token, Bearer 200 + exact body, no-token 401, wrong password 401).

| # | Request | Expected | Actual | Result |
|---|---------|----------|--------|--------|
| 1 | `GET /api/hello` (no token) | 401 | 401 | PASS |
| 2 | `POST /auth/login` `{"username":"user","password":"password"}` | 200 + `{"token":"..."}` | 200 + `{"token":"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiaWF0IjoxNzkzMzYxODMyLCJleHAiOjE3OTMzNjU0MzJ9.GIcmzw9XxqElEpfr12Gn3rm16j-YPFjDkoHTe4W1dx0"}` | PASS |
| 3 | `POST /auth/login` wrong password (`"nope"`) | 401 | 401 | PASS |
| 4 | `GET /api/hello` with `Authorization: Bearer <token from #2>` | 200 + `Hello! JWT authentication successful.` | 200, `Content-Type: text/plain;charset=UTF-8`, body `Hello! JWT authentication successful.` | PASS |
| 5a | `GET /customers` as `user:user123` (Basic) | 200 | 200 | PASS (pt1 regression) |
| 5b | `POST /customers` as `admin:admin123` (Basic), valid payload | 201 | 201 (customer ID 5). Note: first attempt used `jobTitle:"X"` → 400 validation "Job title must be between 2 and 50 characters"; retried with `jobTitle:"Engineer"` → 201. This is a payload-validation issue, not an auth failure. | PASS |
| 5c | `GET /health` (public) | 200 | 200 | PASS |

(The captured token above was issued with the pre-refactor secret, so it will no longer verify —
re-run the matrix to mint fresh tokens.)

### How to reproduce

**Prerequisite:** a module-root `.env` containing `APP_JWT_SECRET`, `APP_USER_USERNAME=user`,
`APP_USER_PASSWORD=password`, `APP_MANAGER_USERNAME`, `APP_MANAGER_PASSWORD`, `APP_ADMIN_USERNAME`, `APP_ADMIN_PASSWORD`, and Postgres access for `simple_crm` (see `.env.example`).

```bash
cd m3-simple-crm/m3-simple-crm
set -a; source .env; set +a  # export credentials for the curl examples
./mvnw spring-boot:run        # dev profile; needs local Postgres simple_crm on localhost:5432

# 1) Protected endpoint without token → expect 401
curl -i http://localhost:8080/api/hello

# 2) Login with the assignment's simple credentials (sourced from .env via app.auth.*) → expect 200 + {"token":"..."}
curl -i -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' -d '{"username":"user","password":"password"}'

# 3) Wrong password → expect 401
curl -i -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' -d '{"username":"user","password":"nope"}'

# 4) Protected endpoint WITH token (paste token from step 2) → expect 200 + hello message
curl -i http://localhost:8080/api/hello -H "Authorization: Bearer <TOKEN>"

# 5) pt1 regression: Basic auth still works alongside JWT
curl -i -u "$APP_USER_USERNAME:$APP_USER_PASSWORD" http://localhost:8080/customers # 200
curl -i -u "$APP_ADMIN_USERNAME:$APP_ADMIN_PASSWORD" -X POST -H 'Content-Type: application/json' \
  -d '{"firstName":"A","lastName":"B","email":"a@b.co","contactNo":"12345678","jobTitle":"Engineer","yearOfBirth":1990}' \
  http://localhost:8080/customers                                 # 201
curl -i http://localhost:8080/health                             # 200 (public)
```

In Postman, the same four requests map to: (1) GET `/api/hello` with no auth → 401; (2) POST
`/auth/login` with the JSON body → 200, copy `token`; (3) set the `Authorization` header to
`Bearer <token>` on GET `/api/hello` → 200 with the hello message.

---

## 4. Notes & caveats

- **`/api/hello` body format (R6):** the endpoint returns a plain `String`, so the response is
  `text/plain` (not JSON). This is the literal reading of the assignment ("returns
  *'Hello! JWT authentication successful.'*"). A grader expecting JSON would see plain text; this is
  documented here per the plan's R6.
- **`jwt.secret` now lives in the gitignored `.env` (formerly R4, deviation 3):** the property
  `jwt.secret` is defined in the profile files as `${APP_JWT_SECRET}` (dev) / a fixed test default
  (test); the literal value is no longer in any tracked file. Fresh clones must copy `.env.example`
  → `.env` and set `APP_JWT_SECRET` (any value ≥ 32 bytes for HS256) before the dev profile will start.
- **Configured credentials:** HTTP Basic and `POST /auth/login` share `APP_USER_*` via
  `app.auth.username` and `app.auth.password`. Basic manager and admin accounts use
  `APP_MANAGER_*` and `APP_ADMIN_*` respectively; JWT login continues to accept only the configured user.
- **Customer permissions:** all three roles may read; only admin may create or delete;
  manager and admin may update. Successful deletion returns HTTP 200 with an empty body.
- **Role mapping (D2):** a JWT-authenticated principal's roles are mapped through the existing
  `UserDetailsService` (configured user → ROLE_USER, manager → ROLE_MANAGER, admin → ROLE_ADMIN); unknown usernames fall back to
  ROLE_USER. JWT users therefore behave identically to Basic users under the pt1 RBAC rules.

---

## 5. References / Credits (J16)

Work referenced while implementing PT2:

- **Spring Security reference documentation** — `SecurityFilterChain`, `authorizeHttpRequests`,
  `SessionCreationPolicy.STATELESS`, `HttpStatusEntryPoint`, and filter ordering
  (`addFilterBefore(..., UsernamePasswordAuthenticationFilter.class)`).
  <https://docs.spring.io/spring-security/reference/servlet/api.html>
- **OWASP JWT Cheat Sheet** — HS256 key strength (≥ 32 bytes / 256 bits), signature and expiry
  validation, and the "validate on every request" principle applied in `JwtAuthFilter`.
  <https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html>
- **jjwt (Java JWT) documentation** — `Jwts.builder()`, `Jwts.parserBuilder()`,
  `SignatureAlgorithm.HS256`, and `Keys.hmacShaKeyFor(...)` used in `JwtService`.
  <https://github.com/jwtk/jjwt>
- **Spring Boot 4 MockMvc test package** — `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`
  (the Boot 4 location of the annotation; the Boot 3 package was intentionally not used).
- **Spring Boot externalised configuration** — `spring.config.import=optional:file:.env[.properties]`
  used to source secrets from the gitignored `.env`.
  <https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config>
- **Classmates / URLs:** none — no classmates were consulted for either phase. Tools and
  documentation/URLs referenced are credited below (per assignment §Submission L56–57).

***Assignment 4.1 & 4.2*** 

| Phase | Scope | Tool |
| :--- | :--- | --- |
| Agents | Session orchestration, MCP/skill loading, document extraction and file management used to run the Plan→Build→Test→Review workflow | Opencode, OMLX |
| Plan | Make a persistent build plan in md file from the requirements (pt1 Basic Auth/RBAC plan + test-fix plan; pt2 JWT auth plan) | Qwen3.8-27B-MLX-4bit | 
| Build | Implement the planning handoffs in source: pom/security config, controllers, JWT service & filter, DTOs, new test classes, secret externalisation to `.env` | Qwen3.8-27B-Coder390-oQ4e-MTP-Text |
| Test | Author/extend JUnit + MockMvc suites, run `./mvnw test`, capture surefire reports, live curl/Postman smoke matrix | Qwen3.8-27B-MLX-4bit, Qwen3.8-27B-Coder390, GLM-5.3-Flash-Free |
| Review | Requirements-to-code reviews: requirement coverage vs `assignment.md`, test-evidence verification, findings + verification-gap reports under `docs/reviews/` | GLM-5.3-Flash-Free |

Tool URLs credited:

- **Opencode** (open-source terminal coding agent) — <https://github.com/sst/opencode>
- **OMLX** (local MLX model serving for the agents) — <https://github.com/Isaac-tools/omlx>
- **Qwen3.8 model family** (plan/build/test models above, MLX 4-bit and MTP variants) — <https://github.com/QwenLM/Qwen3>
- **GLM-5.3-Flash-Free** (test/review model) — <https://github.com/zai-org/GLM>

The documentation URLs each piece of work referenced are listed above
(Spring Security, OWASP JWT Cheat Sheet, jjwt, Spring Boot externalised configuration).
