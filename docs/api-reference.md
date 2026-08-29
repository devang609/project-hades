---
layout: default
title: API Reference
nav_order: 4
---

# API Reference

## Rate Limit Headers

Every proxied response includes rate limit information:

| Header | Description |
|--------|-------------|
| `X-RateLimit-Limit` | Maximum requests allowed in the window |
| `X-RateLimit-Remaining` | Requests remaining in the current window |
| `X-RateLimit-Reset` | Unix epoch seconds when the window resets |
| `Retry-After` | Seconds to wait before retrying (only on 429) |

## HTTP Status Codes

| Code | Meaning |
|------|---------|
| `429 Too Many Requests` | Rate limit exceeded |

## Actuator Endpoints

| Endpoint | Description |
|----------|-------------|
| `GET /actuator/health` | Health check |
| `GET /actuator/prometheus` | Prometheus metrics |
| `GET /actuator/metrics` | Micrometer metrics |
