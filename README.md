# Module 4 Assignment — Dockerize & Deploy (Simple CRM API, Render)

I completed Tasks 1–4 against my **Simple CRM API** (Stretch Option 3 backend) instead of TaskFlow, deployed on Render per the updated assignment requirements.

## Objective
1. simple-crm-api is a Spring Boot 4.1.1 / Java 21 lesson build that **rolls up every lesson in Module 3** (excluding Spring AI), plus Assignments **3.19, 4.1 and 4.2** (unit and integration testing, secure software development & authentication, PT1 and PT2), built on top of the earlier `simple-crm` REST API.

2. End of module assignment requirement is to dockerize & deploy to Render.

## Backend - Deployed

**API URL (Render)**: https://m4-assignment-simple-crm-api.onrender.com

**GitHub**: https://github.com/johnphs-9/module4-deploy-simple-crm

![API Docker Deployed](docs/design/simple-crm-API-arch.png)

**Health Check service at /health** (no authentication required)

![Health check](docs/screenshots/health.png)

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

- Unauthenticated requests → **401** with a `WWW-Authenticate: Basic realm="Realm"` challenge
  header, so a plain browser URL pops Chrome/Firefox's native login dialog.

- authenticated but not authorised (e.g. `user` posting to
`/customers`) → **403**.

### Prerequisites for manual testing

- On startup the `dev` profile drops and recreates the tables 

- `DataLoader` seeds four customers — **1 Tony Stark, 2 Bruce Banner, 3 Peter Parker, 4 Stephen Strange**
(names only; no email/job title). IDs `1…4` below refer to this seed data and assume a fresh
run. 


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
![Render dashboard](screenshots/render-dashboard.png)
![Live API](screenshots/live-api.png)
