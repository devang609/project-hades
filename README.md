<p align="center">
  <h1 align="center">Project Hades</h1>
  <p align="center">Self-hosted distributed API gateway with adaptive rate limiting</p>
</p>

<p align="center">
  <a href="https://github.com/DeVangSharma/gatekeeper/actions/workflows/ci.yml"><img src="https://img.shields.io/github/actions/workflow/status/DeVangSharma/gatekeeper/ci.yml?style=flat-square&label=CI" alt="CI"></a>
  <a href="https://github.com/DeVangSharma/gatekeeper/releases"><img src="https://img.shields.io/github/v/release/DeVangSharma/gatekeeper?style=flat-square" alt="Release"></a>
  <a href="LICENSE"><img src="https://img.shields.io/github/license/DeVangSharma/gatekeeper?style=flat-square" alt="License"></a>
  <img src="https://img.shields.io/badge/Java-21-orange?style=flat-square" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1-green?style=flat-square" alt="Spring Boot 4.1">
</p>

---

## What Is This

Most API gateways treat rate limiting as a counter: X requests per minute, done. Hades goes further — it implements **cost-weighted rate limiting** where expensive endpoints consume more of a client's budget, **behavioral anomaly detection** using probabilistic data structures (Count-Min Sketch + HyperLogLog), and a **learning mode** that observes real traffic patterns and auto-generates rate limit policies. Built on Spring Boot 4.1 WebFlux with Redis for atomic sliding window enforcement via Lua scripting.

## Quick Start

```bash
git clone https://github.com/DeVangSharma/gatekeeper.git
cd gatekeeper
cp docker/.env.example docker/.env
docker compose -f docker/docker-compose.yml up -d
```

| Service   | URL                          |
|-----------|------------------------------|
| Gateway   | http://localhost:8080         |
| Admin API | http://localhost:8090 (future)|

## Features

### Sliding Window Rate Limiting

Redis sorted sets + Lua scripting for atomic, microsecond-precision rate limiting. No bucket boundaries to game — the window slides continuously. Each request gets a unique score, and expired entries are pruned atomically before the count check. The entire operation is a single Lua script execution — zero race conditions under concurrent load.

### WebFlux Reverse Proxy

Pure Spring WebFlux + WebClient reverse proxy with configurable route definitions. Requests are matched by path prefix and forwarded to upstream services with full header and body streaming. No external gateway framework — every line of proxy logic is in the codebase and fully understandable.

### Standard Rate Limit Headers

Every proxied response includes `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset`, and `Retry-After` (on 429). Clients get all the information they need to implement proper backoff without guessing.

### Adaptive Cost-Weighted Rate Limiting *(planned — v0.3.0)*

Not all API calls are equal. A search endpoint that hits an index costs 1x. An export endpoint that joins six tables and streams a CSV costs 20x. Hades assigns cost weights based on upstream response time percentiles and charges the client's rate limit budget accordingly.

### Behavioral Anomaly Detection *(planned — v0.3.0)*

Count-Min Sketch tracks endpoint access patterns per client. HyperLogLog estimates cardinality. Jensen-Shannon divergence detects behavioral shifts — credential stuffing at normal request volume, API enumeration disguised as legitimate traffic. The anomaly detector tightens limits on flagged clients automatically.

### Learning Mode *(planned — v0.5.0)*

Point Hades at your traffic and it computes recommended rate limits: p95 request rates per client tier, cost weight suggestions from latency distributions, anomaly baseline profiles. Review recommendations in the Admin API, approve with one click, and policies go live via Redis hot-reload.

### Real-Time Observability *(planned — v0.4.0)*

Micrometer metrics, Prometheus endpoint, structured logging with trace context. Auto-provisioned Grafana dashboards for gateway health, rate limiting, and per-client views.

## Architecture

```
Client Request
    │
    ▼
┌─────────────────────────────┐
│     Gateway (WebFlux)       │
│   Spring Boot 4.1 / Netty   │
│                             │
│  ┌─────────────────────┐   │
│  │ RateLimitWebFilter   │   │
│  │ (Lua + Redis)        │   │
│  └──────────┬──────────┘   │
│             │               │
│  ┌──────────▼──────────┐   │
│  │  ProxyHandler        │   │
│  │  (WebClient)         │   │
│  └──────────┬──────────┘   │
└─────────────┼───────────────┘
              │
    ┌─────────▼─────────┐
    │  Upstream Service  │
    └───────────────────┘

    ┌───────────────────┐
    │    Redis 7        │
    │  Sorted Sets +    │
    │  Lua Scripting    │
    │  + Pub/Sub        │
    └───────────────────┘
```

<details>
<summary><strong>Technology Map</strong></summary>

| Layer | Technology | Purpose |
|-------|-----------|---------|
| Gateway | Spring Boot 4.1 WebFlux | Reactive request handling (Netty) |
| Reverse Proxy | WebClient | Non-blocking upstream forwarding |
| Rate Limiting | Redis sorted sets + Lua | Atomic sliding window enforcement |
| Event Streaming | Redis Pub/Sub | Rate limit event publishing (future) |
| Configuration | `@ConfigurationProperties` records | Type-safe config with validation |
| Metrics | Micrometer + Prometheus | Dimensional metrics |
| Health | Spring Boot Actuator | Health checks, info endpoint |
| Testing | Testcontainers + JUnit 5 | Integration tests with real Redis |
| Build | Maven multi-module | CI-friendly `${revision}` versioning |
| CI/CD | GitHub Actions | Build, test, publish, deploy docs |
| Containers | Docker + Docker Compose | Local dev and production deployment |

</details>

## Configuration

Copy `docker/.env.example` to `docker/.env` and adjust:

```bash
# Redis connection
REDIS_HOST=localhost
REDIS_PORT=6379

# Rate limiting defaults
RATE_LIMIT_DEFAULT=100    # Requests per window
RATE_LIMIT_WINDOW=60s     # Sliding window duration

# Service ports
GATEWAY_PORT=8080
ADMIN_PORT=8090
```

### Route Configuration

Routes are defined in `application.properties`:

```properties
gatekeeper.routes.definitions[0].id=my-api
gatekeeper.routes.definitions[0].prefix=/api
gatekeeper.routes.definitions[0].target=http://api-service:8080

gatekeeper.routes.definitions[1].id=demo
gatekeeper.routes.definitions[1].prefix=/demo
gatekeeper.routes.definitions[1].target=https://httpbin.org
```

See the [full configuration reference](https://devangsharma.github.io/gatekeeper/configuration) on the docs site.

## Running Tests

```bash
# Unit tests only (no Docker required)
./mvnw verify -pl '!gatekeeper-integration-tests'

# Integration tests (requires Docker for Testcontainers)
./mvnw verify -pl gatekeeper-integration-tests -am

# Everything
./mvnw verify
```

## Development

### Codespaces (One-Click)

Open in GitHub Codespaces — Java 21, Maven, Docker, and Redis are all pre-configured. Run `./mvnw spring-boot:run -pl gatekeeper-gateway` and you're live.

### Local Setup

Requires: Java 21, Docker

```bash
# Start Redis
docker compose -f docker/docker-compose.yml up -d redis

# Build all modules
./mvnw clean verify

# Run the gateway
./mvnw spring-boot:run -pl gatekeeper-gateway
```

## Roadmap

- [x] Maven multi-module scaffold
- [x] WebFlux reactive gateway with WebClient proxy
- [x] Redis sliding window rate limiting (Lua)
- [x] Standard rate limit headers
- [x] Testcontainers integration tests
- [x] CI/CD (GitHub Actions + GHCR)
- [x] Dev Container (Codespaces)
- [ ] Redis Pub/Sub event pipeline
- [ ] Cost-weighted rate limiting
- [ ] Behavioral anomaly detection (Count-Min Sketch + HyperLogLog)
- [ ] Prometheus + Grafana observability
- [ ] Learning mode + Admin API
- [ ] Circuit breaking (custom implementation)
- [ ] Gatling load tests

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for setup instructions and commit conventions.

## License

[Apache 2.0](LICENSE)
