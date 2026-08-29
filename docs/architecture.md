---
layout: default
title: Architecture
nav_order: 5
---

# Architecture

## System Overview

Project Hades is a multi-module Maven project with three deployable services, built entirely on Spring Boot 4.1 — no external gateway frameworks.

```
Client Request
    │
    ▼
┌─────────────────────────────┐
│     Gateway (WebFlux)       │
│   RateLimitWebFilter        │──── Redis (Sliding Window Lua)
│   ProxyHandler (WebClient)  │
└─────────────┬───────────────┘
              │
              ▼
        Upstream Service
```

## Modules

| Module | Runtime | Purpose |
|--------|---------|---------|
| `gatekeeper-gateway` | WebFlux / Netty | Reactive reverse proxy with rate limiting |
| `gatekeeper-analytics` | Spring Boot (virtual threads) | Event consumers via Redis Pub/Sub (future) |
| `gatekeeper-admin` | Spring MVC | REST Admin API (future) |
| `gatekeeper-common` | Library | Shared domain events and DTOs |

## Rate Limiting Algorithm

The sliding window rate limiter uses Redis sorted sets:

1. Each request adds a member scored by timestamp (microseconds)
2. Expired entries outside the window are removed (`ZREMRANGEBYSCORE`)
3. Current count is checked (`ZCARD`)
4. If within limit, the request is added; otherwise rejected
5. All operations execute atomically via a Lua script

This approach provides:
- **Microsecond precision** — no bucket boundaries to game
- **Atomic execution** — no race conditions under concurrent load
- **Automatic cleanup** — TTL ensures memory is bounded

## Reverse Proxy

The gateway proxies requests using Spring's `WebClient`:

1. `RateLimitWebFilter` runs first — checks Redis, returns 429 or continues
2. `ProxyHandler` matches the request path against configured route prefixes
3. The matching route's prefix is stripped and the request is forwarded to the upstream target
4. Response status, headers, and body are streamed back to the client

No framework magic — the proxy logic is ~60 lines of code in `ProxyHandler.java`.
