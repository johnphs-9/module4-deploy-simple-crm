# m3-simple-crm

A Spring Boot 4.1.1 / Java 21 CRM project that **rolls up every lesson in Module 3** (excluding Spring AI), plus
Assignments **4.1 and 4.2** (secure software development & authentication, PT1 and PT2), built
on top of the earlier `simple-crm` REST API.

The main purpose of this repository is to **publish the work done for Assignments 3.19, 4.1 and 4.2**:

| Assignment | Work published here |
|------------|---------------------|
| **3.19 — Software Testing** | JUnit + Mockito unit tests for the service layer, and `@SpringBootTest` + MockMvc integration tests for the REST controller |
| **4.1 — Secure SD & Authentication PT1** | Spring Security with HTTP Basic Auth, BCrypt password encoding, in-memory users, and Role-Based Access Control (RBAC) on `/customers/**` |
| **4.2 — Secure SD & Authentication PT2** | JWT authentication: `POST /auth/login` issuing jjwt HS256 tokens, `JwtAuthFilter` on `/api/**`, and a protected `GET /api/hello` endpoint |

The automated test suite below covers all three assignments, run against the rolled-up project.

---

## Test Summary (Assignments 3.19, 4.1, 4.2)

Run with:

```bash
cd m3-simple-crm/m3-simple-crm
./mvnw test
```

**Result: 39 tests run, 0 failures, 0 errors, 0 skipped — BUILD SUCCESS**
(source: Maven Surefire plugin — the latest run of the rolled-up project).

### Per-class results (from `m3-simple-crm/target/surefire-reports/`)

| Test class | Tests | Failures | Errors | Skipped | Assignment covered |
|------------|------:|---------:|-------:|--------:|--------------------|
| `service.DemoServiceTest` | 10 | 0 | 0 | 0 | 3.19 — unit tests (`@ExtendWith(MockitoExtension.class)`, `@Mock`/`@InjectMocks`) for the service layer (module 3 lessons baseline) |
| `service.CustomerServiceImplTest` | 3 | 0 | 0 | 0 | 3.19 — create / get-by-id / get-by-id-not-found (mock `save()`, `findById()`, `assertThrows` on the custom exception) |
| `controller.CustomerControllerTest` | 9 | 0 | 0 | 0 | 3.19 + 4.1 — MockMvc integration tests for `GET` (200 + JSON array), `POST` with valid data (201), `POST` with invalid data (400), plus HTTP Basic / RBAC coverage (USER allowed, forbidden for non-ADMIN writes) |
| `M3SimpleCrmApplicationTests` | 1 | 0 | 0 | 0 | Baseline — Spring context loads |
| `security.JwtServiceTest` | 7 | 0 | 0 | 0 | 4.2 — subject/`iat`/expiry round-trip, token extraction, valid → true, garbage → false, tampered signature → false, wrong key → false, expired → false |
| `controller.AuthControllerTest` | 4 | 0 | 0 | 0 | 4.2 — `POST /auth/login`: valid credentials → 200 + token; wrong password → 401; wrong username → 401; missing fields → 401 |
| `controller.HelloApiSecurityTest` | 5 | 0 | 0 | 0 | 4.1 + 4.2 — no token → 401; valid Bearer → 200 with expected body; garbage Bearer → 401; expired Bearer → 401; ROLE_USER mapping regression (`GET /customers` → 200) |
| **Total** | **39** | **0** | **0** | **0** | |

### What the suite exercises

- **Assignment 3.19** — unit tests mock the repository (`save`, `findById`) and verify
  create / get-by-id / not-found behaviour; integration tests run through a real MockMvc layer
  asserting status codes, JSON content type and payload validation (200 / 201 / 400).
- **Assignment 4.1 (PT1)** — the real Spring Security filter chain with HTTP Basic auth on the
  in-memory users (`user`/`user123` → ROLE_USER, `admin`/`admin123` → ROLE_ADMIN): public
  `GET /health` permitted, `GET /customers/**` for authenticated users, writes (`POST`/`DELETE` on
  `/customers`) restricted to ADMIN, with the corresponding 401/403/200/201 outcomes asserted.
- **Assignment 4.2 (PT2)** — JWT issuance and validation (`JwtService`, jjwt HS256) and the full
  login → Bearer-token flow through `POST /auth/login` → `GET /api/hello` under the `test`
  profile (H2 in-memory `crm-tests`), ending-to-end without a database.

In addition to the automated suite, a manual curl/Postman verification matrix (login, protected
endpoint with and without a token, and PT1 regression through Basic auth) was run against the live
dev-profile application — all steps passed. Details are recorded in [`m3-simple-crm/TESTING.md`](m3-simple-crm/TESTING.md).

---

## API Reference (all implemented endpoints)

Base URL: `http://localhost:8080` (dev profile). The app uses **two complementary auth mechanisms**:

| Mechanism | Where it applies | How to authenticate |
|-----------|------------------|---------------------|
| **JWT Bearer** (Assignment 4.2) | `POST /auth/login` issues the token; `GET /api/**` requires it | `Authorization: Bearer <TOKEN>` header |
| **HTTP Basic** (Assignment 4.1) | everything else (`/customers/**`, health) | `-u <user>:<pass>` — curl flag or `Authorization: Basic ...` header |

In-memory users: `user`/`user123` → `ROLE_USER`, `admin`/`admin123` → `ROLE_ADMIN`.
`POST /auth/login` instead checks the credentials configured via `app.auth.username` /
`app.auth.password` in `.env` (assignment defaults `user`/`password`) — note these are
**independent** of the Basic-auth users above.

### Endpoint summary

| Method | Path | Auth | Roles | Success | Notes |
|--------|------|------|-------|---------|-------|
| `GET` | `/health` | none (public) | — | 200 `{"status":"UP"}` | liveness probe |
| `POST` | `/auth/login` | none (public) | — | 200 `{"token":"…"}` | 401 on bad/missing credentials |
| `GET` | `/api/hello` | JWT | any authenticated | 200 plain string | PT2 protected endpoint |
| `GET` | `/customers` | Basic | USER, ADMIN | 200 JSON array | |
| `GET` | `/customers/{id}` | Basic | USER, ADMIN | 200 | 404 if not found |
| `GET` | `/customers/search?firstName=…` | Basic | USER, ADMIN | 200 JSON array | exact-match on first name |
| `GET` | `/customers/search/job?jobTitle=…` | Basic | USER, ADMIN | 200 JSON array | JPQL exact-match on job title |
| `POST` | `/customers` | Basic | **ADMIN only** | 201 created customer | 400 on validation errors |
| `PUT` | `/customers/{id}` | Basic | **ADMIN only** | 200 updated customer | full replace; 404 if not found |
| `DELETE` | `/customers/{id}` | Basic | **ADMIN only** | 204 no body | 404 if not found |
| `POST` | `/customers/{id}/interactions` | Basic | **ADMIN only** | 201 created interaction | falls back to `POST /customers/**` rule; 404 if customer missing |

Unauthenticated requests → **401**; authenticated but not authorised (e.g. `user` posting to
`/customers`) → **403**.

### Prerequisites for manual testing

1. A module-root `.env` (see the inner `.env.example`) with at minimum:
   `APP_JWT_SECRET`, `APP_USER_USERNAME=user`, `APP_USER_PASSWORD=password`.
2. A running local PostgreSQL database `simple_crm` on `localhost:5432`
   (dev profile; create the DB once with `createdb simple_crm`).
3. Start the app:

   ```bash
   cd m3-simple-crm/m3-simple-crm
   ./mvnw spring-boot:run
   ```

   On startup the `dev` profile drops and recreates the tables and
   [`DataLoader`](m3-simple-crm/src/main/java/sg/edu/ntu/m3_simple_crm/service/DataLoader.java)
   seeds four customers — **1 Tony Stark, 2 Bruce Banner, 3 Peter Parker, 4 Stephen Strange**
   (names only; no email/job title). IDs `1…4` below refer to this seed data and assume a fresh
   run. Tip: run `curl http://localhost:8080/customers -u user:user123` first to confirm your real IDs.

### Customer JSON shape

```json
{
  "id": 1,
  "firstName": "Tony",
  "lastName": "Stark",
  "email": "tony@stark.com",
  "contactNo": "12345678",
  "jobTitle": "Engineer",
  "yearOfBirth": 1970
}
```

Validation (enforced server-side, returns 400): `firstName` and `lastName` are mandatory
(non-blank), `email` must be a valid email format, `contactNo` must be exactly 8 characters,
`jobTitle` must be 2–50 characters.

### Getting a JWT for the `/api/**` endpoints

Requires `jq` (or paste the token manually):

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"user","password":"password"}' | jq -r .token)
echo "$TOKEN"   # a JWT valid for 1 hour (jwt.expiration-ms=3600000)
```

---

## Manual testing with curl (endpoint by endpoint)

Each step notes the expected response. Add `-i` to any command to see the status line and headers.

### 1. `GET /health` — public liveness probe

```bash
curl http://localhost:8080/health
# → 200, body: {"status":"UP"}
```

### 2. `POST /auth/login` — obtain a JWT

```bash
curl -i -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"user","password":"password"}'
# → 200, body: {"token":"<signed HS256 JWT>"}
```

Negative cases:

```bash
curl -o /dev/null -w '%{http_code}\n' -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' -d '{"username":"user","password":"wrong"}'        # → 401
curl -o /dev/null -w '%{http_code}\n' -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' -d '{"username":"nobody","password":"password"}'   # → 401
curl -o /dev/null -w '%{http_code}\n' -X POST http://localhost:8080/auth/login           # → 401 (no body)
```

### 3. `GET /api/hello` — JWT-protected endpoint

```bash
# Without a token → 401 (never ask for Basic auth on /api/**)
curl -o /dev/null -w '%{http_code}\n' http://localhost:8080/api/hello                 # → 401

# With a garbage token → 401 (JwtService rejects the signature)
curl -o /dev/null -w '%{http_code}\n' http://localhost:8080/api/hello \
  -H 'Authorization: Bearer not.a.jwt'                                               # → 401

# With the token from step 2 → 200
curl http://localhost:8080/api/hello -H "Authorization: Bearer $TOKEN"
# → 200, body: Hello! JWT authentication successful.
```

### 4. `GET /customers` — list all customers (Basic auth, any role)

```bash
curl http://localhost:8080/customers -u user:user123
# → 200, JSON array of the 4 seeded customers

# Unauthenticated → 401
curl -o /dev/null -w '%{http_code}\n' http://localhost:8080/customers                 # → 401
```

### 5. `GET /customers/{id}` — get one customer

```bash
# IDs reset every startup (DataLoader + ddl-auto=create): 1=Tony Stark … 4=Stephen Strange
curl http://localhost:8080/customers/1 -u user:user123
# → 200, body: {"id":1,"firstName":"Tony","lastName":"Stark","email":null,...}

curl -i http://localhost:8080/customers/999 -u user:user123
# → 404, body: {"message":"Exception: Could not find customer with id: 999","timestamp":"2026-11-07T10:00:00.123"}
```

### 6. `POST /customers` — create a customer (ADMIN only)

```bash
# Valid payload → 201 with the created customer (server-assigned id)
curl -i -X POST http://localhost:8080/customers -u admin:admin123 \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Diana","lastName":"Prince","email":"diana@prince.com","contactNo":"87654321","jobTitle":"Archivist","yearOfBirth":1985}'
# → 201, body: {"id":5,"firstName":"Diana",...}

# ROLE_USER → 403 (writes are ADMIN-only)
curl -o /dev/null -w '%{http_code}\n' -X POST http://localhost:8080/customers -u user:user123 \
  -H 'Content-Type: application/json' -d '{"firstName":"A","lastName":"B"}'           # → 403

# Bad email + short contactNo → 400 with the joined validation messages
curl -i -X POST http://localhost:8080/customers -u admin:admin123 \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Bruce","lastName":"Wayne","email":"not-an-email","contactNo":"123"}'
# → 400, body: {"message":"Email should be valid. Contact number must be exactly 8 characters",...}
```

### 7. `PUT /customers/{id}` — replace a customer (ADMIN only)

`PUT` copies **all** fields from the body; omitted fields are written as `null`/`0` — it is a full
replace, not a partial patch, and the path `id` overrides any `id` in the body.

```bash
curl -i -X PUT http://localhost:8080/customers/1 -u admin:admin123 \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Anthony","lastName":"Stark","email":"anthony@stark.com","contactNo":"11111111","jobTitle":"CEO","yearOfBirth":1970}'
# → 200, body: customer 1 with all six fields replaced (its id is preserved)

curl -i -X PUT http://localhost:8080/customers/999 -u admin:admin123 \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Ghost","lastName":"Person"}'
# → 404
```

### 8. `DELETE /customers/{id}` — delete a customer (ADMIN only)

```bash
curl -i -X DELETE http://localhost:8080/customers/5 -u admin:admin123
# → 204 (no body); a subsequent GET /customers/5 returns 404

curl -i -X DELETE http://localhost:8080/customers/999 -u admin:admin123   # → 404
curl -o /dev/null -w '%{http_code}\n' -X DELETE http://localhost:8080/customers/5 -u user:user123   # → 403 (ADMIN only)
```

### 9. `POST /customers/{id}/interactions` — add an interaction (ADMIN only)

```bash
curl -i -X POST http://localhost:8080/customers/1/interactions -u admin:admin123 \
  -H 'Content-Type: application/json' \
  -d '{"remarks":"Followed up on project requirements","interactionDate":"2026-11-01"}'
# → 201, body: {"id":1,"remarks":"Followed up on project requirements","interactionDate":"2026-11-01","customer":{"id":1,...}}

# Missing customer → handled by the global exception handler
curl -o /dev/null -w '%{http_code}\n' -X POST http://localhost:8080/customers/999/interactions \
  -u admin:admin123 -H 'Content-Type: application/json' \
  -d '{"remarks":"test","interactionDate":"2026-11-01"}'                              # → 404

# ROLE_USER → 403 (POST under /customers/** is ADMIN-only)
curl -o /dev/null -w '%{http_code}\n' -X POST http://localhost:8080/customers/1/interactions \
  -u user:user123 -H 'Content-Type: application/json' -d '{"remarks":"test"}'         # → 403
```

### 10. `GET /customers/search?firstName=…` — exact first-name search

```bash
curl 'http://localhost:8080/customers/search?firstName=Tony' -u user:user123
# → 200, JSON array — [{"id":1,"firstName":"Tony","lastName":"Stark",...}]

curl -o /dev/null -w '%{http_code}\n' 'http://localhost:8080/customers/search?firstName=OAA' -u user:user123   # → 200 + [] (empty array, not 404)
```

### 11. `GET /customers/search/job?jobTitle=…` — job-title search (JPQL)

The seeded customers have no job title, so create one first (step 6), then:

```bash
curl 'http://localhost:8080/customers/search/job?jobTitle=Archivist' -u user:user123
# → 200, JSON array containing Diana Prince (returns [] for unknown titles)
```

### 12. One command re-tests the whole matrix

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"user","password":"password"}' | jq -r .token) \
&& curl -s  http://localhost:8080/health | jq .                                            \
&& curl -s  http://localhost:8080/api/hello                | head -1                       \
&& curl -s  http://localhost:8080/api/hello -H "Authorization: Bearer $TOKEN" | head -1    \
&& curl -s  http://localhost:8080/customers -u user:user123      | jq length               \
&& curl -s  http://localhost:8080/customers/1 -u user:user123    | jq -c .                 \
&& curl -s  -X POST http://localhost:8080/customers -u admin:admin123 \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Diana","lastName":"Prince","email":"diana@prince.com","contactNo":"87654321","jobTitle":"Archivist","yearOfBirth":1985}' | jq -c . \
&& curl -s  -X PUT http://localhost:8080/customers/1 -u admin:admin123 \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Anthony","lastName":"Stark","email":"anthony@stark.com","contactNo":"11111111","jobTitle":"CEO","yearOfBirth":1970}' | jq -c . \
&& curl -s  -X POST http://localhost:8080/customers/1/interactions -u admin:admin123 \
  -H 'Content-Type: application/json' \
  -d '{"remarks":"Followed up on project requirements","interactionDate":"2026-11-01"}' | jq -c . \
&& curl -s  'http://localhost:8080/customers/search?firstName=Tony' -u user:user123 | jq -r '.[].firstName' \
&& curl -s  'http://localhost:8080/customers/search/job?jobTitle=Archivist' -u user:user123 | jq -r '.[].firstName' \
&& curl -sI -X DELETE http://localhost:8080/customers/5 -u admin:admin123 | head -1
```

**Run checklist (all green)**: `health` → `{"status":"UP"}` · `hello` without/with token → 401
message then `Hello! JWT authentication successful.` · `customers` → array length (4 fresh) ·
POST/PUT/interactions echo the persisted JSON · both searches → matching names · DELETE → `HTTP/1.1 204`.

---

## Credits (from TESTING.md — References / Credits, J16)

### References

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
- **Classmates / URLs:** none — no classmates were consulted. Tools and documentation/URLs
  referenced are credited below (per assignment §Submission L56–57).

### AI tool credits — Assignments 4.1 & 4.2

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
