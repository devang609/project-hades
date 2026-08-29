# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.0-alpha] - 2026-08-29

### Added
- Maven multi-module project scaffold with CI-friendly `${revision}` versioning
- Spring Boot 4.1 WebFlux reactive gateway with WebClient-based reverse proxy
- Redis sliding window rate limiting via Lua scripting (sorted sets)
- Custom `RateLimitWebFilter` executing Lua atomically via `ReactiveRedisTemplate`
- Configurable route definitions with prefix-based matching and upstream proxying
- Standard rate limit response headers (`X-RateLimit-Limit`, `Remaining`, `Reset`, `Retry-After`)
- Testcontainers integration test verifying atomicity under concurrent load
- GitHub Actions CI workflow (build + test on every push/PR)
- GitHub Actions release workflow (tag-triggered: GHCR publish + GitHub Release)
- CodeQL security scanning (weekly + on PR)
- GitHub Pages docs site (Jekyll + just-the-docs)
- Dependabot configuration for Maven, GitHub Actions, and Docker ecosystems
- Dev Container for one-click Codespaces setup
- Docker Compose with Redis for local development
- Apache 2.0 license

[Unreleased]: https://github.com/DeVangSharma/gatekeeper/compare/v0.1.0-alpha...HEAD
[0.1.0-alpha]: https://github.com/DeVangSharma/gatekeeper/releases/tag/v0.1.0-alpha
