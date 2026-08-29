---
layout: default
title: Getting Started
nav_order: 2
---

# Getting Started

## Prerequisites

- Docker and Docker Compose
- Java 21 (for development)
- Maven (wrapper included, no global install needed)

## Quick Start

```bash
git clone https://github.com/devang609/project-hades.git
cd project-hades
cp docker/.env.example docker/.env
docker compose -f docker/docker-compose.yml up -d
```

The gateway is now running at `http://localhost:8080`.

## Test It

```bash
# Send a request through the gateway
curl -H "X-API-Key: demo-client" http://localhost:8080/demo/get

# Check rate limit headers in the response
curl -v -H "X-API-Key: demo-client" http://localhost:8080/demo/get 2>&1 | grep X-RateLimit
```

## Development Setup

### Using Codespaces (Recommended)

Click "Code" → "Codespaces" → "Create codespace on main". Everything is pre-configured.

### Local Development

```bash
# Start infrastructure
docker compose -f docker/docker-compose.yml up -d redis

# Build all modules
./mvnw clean verify

# Run the gateway
./mvnw spring-boot:run -pl gatekeeper-gateway
```

## Ports

| Service    | Port |
|------------|------|
| Gateway    | 8080 |
| Admin API  | 8090 |
| Grafana    | 3000 |
| Prometheus | 9090 |
