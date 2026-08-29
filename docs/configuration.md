---
layout: default
title: Configuration
nav_order: 3
---

# Configuration Reference

Project Hades is configured via environment variables and Spring application properties.

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `REDIS_HOST` | `localhost` | Redis server hostname |
| `REDIS_PORT` | `6379` | Redis server port |
| `RATE_LIMIT_DEFAULT` | `100` | Default requests per window |
| `RATE_LIMIT_WINDOW` | `60s` | Default sliding window duration |

## Rate Limiting

Rate limits use a sliding window algorithm backed by Redis sorted sets and Lua scripting for atomicity. Each request is scored and tracked within the window.

## Route Configuration

Routes are defined via `@ConfigurationProperties` in `application.yml`:

```yaml
gatekeeper:
  routes:
    definitions:
      - id: my-api
        prefix: /api
        target: http://api-service:8080
      - id: demo
        prefix: /demo
        target: https://httpbin.org
```

Each route has:
- `id` — unique identifier for logging and metrics
- `prefix` — path prefix to match incoming requests
- `target` — upstream base URL to proxy to (prefix is stripped)

Routes are matched in order — first match wins.

## Client Identification

Clients are identified in this priority order:
1. `X-API-Key` header
2. `X-Forwarded-For` header (first IP)
3. Falls back to `anonymous`
