# Repository diagram handoff (Mermaid)

Created 1 November 2026 (Asia/Singapore). Diagrams were grounded in a fresh inspection of the current repository; the earlier draw.io handoff (`m3-simple-crm-diagrams-codex-20261031-122139.md`) referenced files that no longer exist in `docs/design/` (now empty).

## Deliverables

Nine Mermaid sources in `docs/design/` (user requested Mermaid output only; no SVG/draw.io exports):

| View | File |
|---|---|
| Architecture | `m3-simple-crm-technical-architecture-qwen3.8-27b-mlx-4bit-20261101-152746.mmd` |
| Sequence — login | `m3-simple-crm-sequence-login-qwen3.8-27b-mlx-4bit-20261101-152746.mmd` |
| Sequence — JWT-protected access | `m3-simple-crm-sequence-protected-access-qwen3.8-27b-mlx-4bit-20261101-152746.mmd` |
| Sequence — customer create | `m3-simple-crm-sequence-customer-create-qwen3.8-27b-mlx-4bit-20261101-152746.mmd` |
| Sequence — customer get by id | `m3-simple-crm-sequence-customer-get-qwen3.8-27b-mlx-4bit-20261101-152746.mmd` |
| Sequence — interaction create | `m3-simple-crm-sequence-interaction-create-qwen3.8-27b-mlx-4bit-20261101-152746.mmd` |
| Class — domain & persistence | `m3-simple-crm-class-domain-qwen3.8-27b-mlx-4bit-20261101-152746.mmd` |
| Class — web & security | `m3-simple-crm-class-web-security-qwen3.8-27b-mlx-4bit-20261101-152746.mmd` |
| ER — database entities | `m3-simple-crm-class-entities-er-qwen3.8-27b-mlx-4bit-20261101-152746.mmd` |

## Source evidence

Paths relative to `m3-simple-crm/` (Maven module). `src/main/java/sg/edu/ntu/m3_simple_crm/` prefix omitted.

| View | Evidence |
|---|---|
| Architecture | `pom.xml`, `M3SimpleCrmApplication`, `config/SecurityConfig`, all four controllers, service classes, repositories, `application.properties`, `application-dev.properties`, `application-prod.properties` |
| Login | `auth/AuthController`, `auth/LoginRequest`, `auth/TokenResponse`, `security/JwtService` |
| Protected access | `security/JwtAuthFilter`, `config/SecurityConfig`, `controller/HelloController`, `src/test/.../HelloApiSecurityTest` |
| Customer create | `controller/CustomerController`, `service/CustomerServiceImpl`, `repository/CustomerRepository`, `src/test/.../CustomerControllerTest` |
| Customer get | `service/CustomerServiceImpl` (Optional -> not-found), `exceptions/GlobalExceptionHandler`, `exceptions/CustomerNotFoundException`, `exceptions/ErrorResponse` |
| Interaction create | `controller/CustomerController` (addInteractionToCustomer), `service/CustomerServiceImpl`, `model/Interaction` |
| Entities | `model/Customer`, `model/Interaction`, `model/Course`, `model/Student` |

Non-compiled `repository/CustomerRepositoryImpl.java.old` and `service/CustomerServiceWithLoggingImpl.java.old` were treated as stale, not implementation.

## Decisions

- One file per diagram using the skill naming convention with model slug `qwen3.8-27b-mlx-4bit`.
- Sequence diagrams stay at the runtime boundary level: Spring Data JPA/Hibernate is collapsed into repository/database participants; no invented internals.
- Class diagram split into domain/persistence and web/security to keep each readable; foreign-key associations shown without inventing composition or cascade deletion.
- `DemoService` is a plain POJO (no Spring annotation) and is shown as a non-bean utility exercised by unit tests only.
- `Course`/`Student` + `CourseRepository` are implemented entities with no REST API or service usage; shown in class/ER views with that caveat.
- `JwtAuthFilter` is instantiated inline in `SecurityConfig` (not a bean); class diagram uses a dashed link for that `new` relationship.

## Validation status

- All nine `.mmd` files were validated with `mmdc` (mermaid-cli 12.0.0, local headless Chrome; SVG parse + PNG render), and every render was then visually inspected from its PNG.
- One syntax defect was found and fixed in the architecture file: class-diagram-style `edge : "label"` syntax is invalid in `flowchart`. Nine edge labels were converted to the `-- "label" -->` / `-. "label" .->` forms and the file re-validated.
- Cosmetic, not fixed: in `class-domain` the two FK association labels render beside the empty repository boxes, and in the architecture diagram the `.env` dashed-edge labels render near the `.env` node. Semantics are unaffected.
- Application source was not modified; no build/test run was performed.

## Next agent task

If runtime behavior changes, re-trace the relevant source before revising these diagrams. Re-run `mmdc` on any edited `.mmd` before shipping.
