# Inky Gallery

An app for creating drawings and viewing them on an InkyFrame.

## Development

Things to install:
- Docker
- IntelliJ IDEA for Backend
  - Java 21
- Visual Studio Code for Frontend/Micropython
- NVM to install Node 22
- Optional Thonny for Micropython
- pgadmin to explore database

Setup steps:

- Git commit hooks
  - `./scripts/setup-git-hooks.sh`

- Set up .env (see `.env.example`)
- Get postgres running locally with docker
  - run `docker compose -f docker-compose.dev.yml up -d postgres`

Reset Database (will delete everything):
- `docker compose -f docker-compose.dev.yml down -v`
- `docker compose -f docker-compose.dev.yml up -d postgres`


## Structure

### backend

Built in Kotlin and http4k.

Serves the API and built frontend assets.

### frontend

Built in Typescript/React.

### inkyframe

Built in micropython, to be deployed on an InkyFrame.

