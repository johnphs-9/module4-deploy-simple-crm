# Module 4 Assignment — Dockerize & Deploy (Simple CRM API, Render)

I completed Tasks 1–4 using my **Simple CRM API** (Stretch Option 3 backend) instead of TaskFlow, deployed on Render per assignment requirements.

For the optional stretch goal, I added **Stretch Option 1 — a CI pipeline** that builds, tests and publishes the Docker image to Docker Hub on every push to `main` (see [Stretch S1 — CI pipeline to Docker Hub](#stretch-s1--ci-pipeline-to-docker-hub-github-actions)).

## Objective
1. simple-crm-api is a Spring Boot 4.1.1 / Java 21 lesson build that **rolls up every lesson in Module 3** (excluding Spring AI), plus Assignments **3.19, 4.1 and 4.2** (unit and integration testing, secure software development & authentication, PT1 and PT2), built on top of the earlier `simple-crm` REST API.

2. End of module assignment requirement is to dockerize & deploy to Render.

## Backend - deployed

**API URL (Render)**: https://m4-assignment-simple-crm-api.onrender.com

**GitHub**: https://github.com/johnphs-9/module4-deploy-simple-crm

![API Docker Deployed](docs/design/simple-crm-API-arch.png)

---

## Stretch S1 — CI pipeline to Docker Hub (GitHub Actions)

Per assignment **Stretch Option 1**, I added a GitHub Actions CI pipeline (`.github/workflows/docker.yml`) that runs on **every push to `main`** so that a **CI pipeline** to Docker Hub and the Render **deployment** pipeline work side by side:

| Pipeline | Trigger | What it does |
| --- | --- | --- |
| **CI pipeline** (GitHub Actions) | push to `main` | Builds with Maven → runs all unit & integration tests (H2 in-memory, no DB needed in CI) → on test success builds the Docker image (`simple-crm-api/` context) and pushes it to Docker Hub |
| **Deployment pipeline** (Render) | push to `main` | Builds the Dockerfile and deploys the web service on Render |

### Pipeline design

- **Job 1 — `test`:** `actions/checkout` → `actions/setup-java` (Temurin 21, Maven cache) → `./mvnw test` (all tests use the `test` profile backed by an H2 in-memory database).
- **Job 2 — `publish`:** runs **only if tests pass** (`needs: test`) → `docker/login-action` authenticated with GitHub repository **secrets** `DOCKERHUB_USERNAME` and `DOCKERHUB_TOKEN` → `docker/build-push-action` with `context: simple-crm-api` and `push: true`.
- **Docker Hub credentials are stored as GitHub repository secrets — never committed to code** (assignment requirement verified: `git ls-files` contains no tokens or passwords).
- **Image tags pushed each run:**
  - `johnphs9/module4-deploy-simple-crm:latest` — always points at the newest passing build,
  - `johnphs9/module4-deploy-simple-crm:g<short-SHA>` — one immutable tag per commit for traceability/rollback.

### Links

- Successful pipeline run: **<to be added after first run — GitHub Actions run link>**
- Image on Docker Hub: <https://hub.docker.com/r/johnphs9/module4-deploy-simple-crm/tags>

---

## Spring Security 

### Authentication and Authorisation (RBAC)

| Request | user | manager | admin |
|---|---|---|---|
| username | _user_ | _manager_ | _admin_ |
| password | _user123_ | _manager123_ | _admin123_ | 
| GET `/customers` | ✅ 200 | ✅ 200 | ✅ 200 |
| GET `/customers/{id}` | ✅ 200 | ✅ 200 | ✅ 200 |
| POST `/customers` | ❌ 403 | ❌ 403 | ✅ 201 |
| PUT `/customers/{id}` | ❌ 403 | ✅ 200 | ✅ 200 |
| DELETE `/customers/{id}` | ❌ 403 | ❌ 403 | ✅ 200 |

### Suggested shell variables to set (export) for manual testing using curl

```text
APP_USER_USERNAME=user
APP_USER_PASSWORD=user123
APP_ADMIN_USERNAME=admin
APP_ADMIN_PASSWORD=admin123
APP_MANAGER_USERNAME=manager
APP_MANAGER_PASSWORD=manger123

BASE="https://m4-assignment-simple-crm-api.onrender.com"

TOKEN="<token string xxx.yyy.zzz (JWT bearer token format) from successful login>"
```

---

## API Reference (all implemented endpoints)

Base URL: `https://m4-assignment-simple-crm-api.onrender.com` (dev profile). 

The app uses **two complementary auth mechanisms**:

| Mechanism | Where it applies | How to authenticate |
|-----------|------------------|---------------------|
| **JWT Bearer** (Assignment 4.2) | `POST /auth/login` issues the token; `GET /api/**` requires it | `Authorization: Bearer <TOKEN>` header |
| **HTTP Basic** (Assignment 4.1) | everything else (`/customers/**`, health) | `-u <user>:<pass>` — curl flag or `Authorization: Basic ...` header |

**In-memory users**: 
- `user`/`user123` → `ROLE_USER`
-  `manager`/`manager123` → `ROLE_MANAGER`
- `admin`/`admin123` → `ROLE_ADMIN`

All three accounts can obtain a JWT at `POST /auth/login`

### Endpoint summary

| Method | Path | Auth | Roles | Success | curl |
|--------|------|------|-------|---------|:-------|
| `GET` | `/health` | **none (public)** | — | 200 `{"status":"UP"}` | `curl $BASE/health` |
| `POST` | `/auth/login` | **none (public)** | — | 200 `{"token":"…"}`, 401 on bad/missing credentials | `curl -X POST $BASE/auth/login -H 'Content-Type: application/json' -d '{"username":"user", "password":"user123"}'` |
| `GET` | `/api/hello` | JWT | ANY | 200 plain string | `curl $BASE/api/hello -H "Authorization: Bearer $TOKEN"` |
| `GET` | `/customers` | Basic | ANY | 200 JSON array | `curl $BASE/customers -u manager:manager123` |
| `GET` | `/customers/{id}` | Basic | ANY | 200, 404 if not found | `curl $BASE/customers/1 -u user:user123` |
| `GET` | `/customers/search?firstName=…` | Basic | ANY | 200 JSON array, exact-match on first name | `curl "$BASE/customers/search?firstName=Stephen" -u user:user123` |
| `GET` | `/customers/search/job?jobTitle=…` JPQL exact-match on job title | Basic | ANY | 200 JSON array | `curl "$BASE/customers/search/job?jobTitle=CEO" -u user:user123` |
| `POST` | `/customers` | Basic | ADMIN | 201 created customer, 400 on validation errors  | `curl -i -X POST $BASE/customers -u admin:admin123 -H 'Content-Type: application/json' -d '{"firstName":"Diana","lastName":"Prince","email":"diana@prince.com","contactNo":"87654321","jobTitle":"Archivist","yearOfBirth":1985}'`|
| `PUT` | `/customers/{id}` | Basic | MANAGER, ADMIN | 200 updated customer, full replace; 404 if not found | `curl -i -X PUT $BASE/customers/1 -u admin:admin123 -H 'Content-Type: application/json' -d '{"firstName":"Anthony","lastName":"Stark","email":"anthony@stark.com","contactNo":"11111111","jobTitle":"CEO","yearOfBirth":1970}'`|
| `DELETE` | `/customers/{id}` | Basic | ADMIN | 204 no body, 404 if not found | `curl -i -X DELETE $BASE/customers/5 -u admin:admin123` |
| `POST` | `/customers/{id}/interactions` | Basic | ADMIN | 201 created interaction, 404 if customer missing | `curl -i -X POST $BASE/customers/1/interactions -u admin:admin123 -H 'Content-Type: application/json' -d '{"remarks":"Followed up on project requirements","interactionDate":"2026-11-01"}'` |

- Unauthenticated requests → **401** with a `WWW-Authenticate: Basic realm="Realm"` challenge header → Chrome/Firefox pops up a native login dialog.

- Authenticated but not authorised → **403**.

### Database Data Refresh  

- The `dev` profile is set for update. Tables and data are persistent across service restarts. 

- `DataLoader` seeds four customers at service startup if customer repository is empty — **1 Tony Stark, 2 Bruce Banner, 3 Peter Parker, 4 Stephen Strange**
(names only; no email/job title). 

(these are  sample customer IDs; they are not guaranteed to be 1..4 in sequence when seeding an empty table in an existing database)



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

Validation (enforced server-side, returns 400): 
- `firstName` and `lastName` are mandatory (non-blank)
- `email` must be a valid email format 
- `contactNo` must be exactly 8 characters,
- `jobTitle` must be 2–50 characters.

---

## Screenshots

### Render Dashboard

![Render dashboard](docs/screenshots/Render%20Project%20.png)

### Webservice and PostgreSQL deployed successfully

- A successful `push main` to GitHub automatically triggers a docker build in Render.

![Render dashboard](docs/screenshots/Render%20Web%20Service.png)

- logs showing simple-crm-api deployed successfully and serving requests - performing CRUD operations on the database.

![Render service logs](docs/screenshots/Render%20webservice%20log.png)

### Health Check service (no authentication required)

![Health check](docs/screenshots/health.png)

### Browser native login prompt

- When a protected endpoint is requested without valid credentials, the API returns a 401 Unauthorized response with a Basic authentication challenge. 
- The browser then displays its native login prompt.

![browser login](docs/screenshots/Browser%20native%20login%20prompt.png)

### Live API 

- Endpoint: [GET /customers](https://m4-assignment-simple-crm-api.onrender.com/customers)
- Browser then supplies the previously collected credentials in an `Authorization: Basic` header on subsequent requests.

![Live API](docs/screenshots/Get_customers_authenticated.png)

### Service waking 

- Render will spin down instances for free tier accounts after a period of inactivity (~15 minutes).    

- Render automatically restarts the service upon incoming requests. The entire waking up process may take up to 2 minutes as both web and database services are brought up. 

![Render service waking](docs/screenshots/Render_service_waking.png)

- Logs showing service successfully brought up. 

![Render service waking logs](docs/screenshots/Render_service_wake_logs.png)

- Response from API as expected after service running again.

![Request served after service awake](docs/screenshots/Get_awake_result.png)

- Database data remains persisted across service restarts.

![Data persistence after waking](docs/screenshots/Get_awake_data_persistence.png)
