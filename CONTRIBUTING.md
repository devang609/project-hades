# Contributing

Contributions welcome. Here's how:

## Setup

Open in [Codespaces](../../codespaces/new) or locally:
- Java 21, Docker, Maven (wrapper included)
- `docker compose -f docker/docker-compose.yml up -d` for infrastructure
- `mvn verify` to build and test

## Commits

[Conventional Commits](https://www.conventionalcommits.org/) format:
`type(scope): description`

Types: `feat`, `fix`, `perf`, `docs`, `test`, `refactor`, `chore`
Scopes: `gateway`, `analytics`, `admin`, `common`, `docker`, `ci`

## Pull Requests

- Branch from `develop`
- Ensure `mvn verify` passes
- One feature per PR

## Questions

Open an issue or start a Discussion.
