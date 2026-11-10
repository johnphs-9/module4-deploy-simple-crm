# Changelog

All notable changes to the Simple CRM API Docker image are documented here.

Format based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/); versioning follows
[Semantic Versioning](https://semver.org/) (MAJOR.MINOR.PATCH — Lesson 4.5). The active version
number lives in the root `VERSION` file, which the CI pipeline reads to tag Docker images
(see `.github/workflows/docker.yml`).

| Pipeline | Version | Docker image tag(s) |
| --- | --- | --- |
| Docker Hub (`johnphs9/module4-deploy-simple-crm`) | the versions below | `<version>`, `sha-<short-SHA>` (legacy `g<short-SHA>`), `latest` |
| Render (`m4-assignment-simple-crm-api`) | deploys the same source | built directly from the Dockerfile on push to `main` |

## [1.1.0] — 2026-11-10

### Added

- **Stretch Option 4 — versioned Docker images.** The CI publish job reads the root `VERSION`
  file and tags the image with the `MAJOR.MINOR.PATCH` release version; the pipeline fails if
  the file is missing or malformed (no unversioned image can be published by accident).
- Historical versions backfilled on Docker Hub: `1.0.2` and `1.0.3` (local builds pushed
  manually), `1.0.4` (CI build retagged).
- Root `CHANGELOG.md` and README versioning-scheme documentation, plus the Docker Hub
  tags-page screenshot.

## [1.0.4] — 2026-11-10

### Added

- **Stretch Option 1 — CI pipeline** (`.github/workflows/docker.yml`): builds with Maven, runs
  all unit & integration tests (H2 in-memory), then publishes the image to Docker Hub on every
  push to `main`, side by side with the Render deployment pipeline.
- Docker Hub credentials supplied as GitHub repository secrets (never committed).

## [1.0.3] — 2026-11-09

### Added

- **Customer data preserved across service restarts**: `DataLoader` seeds the four sample
  customers only when the repository is empty (no more wipe-and-reseed on every startup).

### Changed

- Logging improvements, README and screenshot updates for submission.

## [1.0.2] — 2026-11-08

### Added

- `BasicAuthenticationEntryPoint` so the API's 401 challenge lets Chrome/Firefox show the
  native login prompt (previously unauthenticated requests got no browser dialog).

### Changed

- Assignment README updated for submission.

## [1.0.1] — 2026-11-08

### Fixed

- JWT login: all three defined accounts (user / manager / admin) can obtain a token at
  `POST /auth/login`.

### Changed

- PostgreSQL container port switched to 5432; local documentation updated.

> Pre-Docker-Hub local builds — versions `1.0.1` and `1.0.2` were built and deployed to Render
> locally before the Docker Hub repository existed; their images survive as the Hub tags
> `1.0.2` / `1.0.3` pushed in the 1.1.0 backfill.
