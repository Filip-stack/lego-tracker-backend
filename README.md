# LEGO Star Wars Tracker (Backend)

REST API for managing LEGO Star Wars sets and their store offers. Set details can be fetched from Rebrickable, while affiliate feed data is parsed and synchronized with PostgreSQL.

## Tech stack

- Java and Spring Boot
- Spring Web, Spring Data JPA, and scheduled tasks
- PostgreSQL with Docker Compose
- Maven
- Jackson XML/JSON
- OpenAPI / Swagger UI

## Features

- Create, list, search, fetch, and delete LEGO sets.
- Fetch an individual set or import the Star Wars theme catalog from Rebrickable, following paginated API results.
- List and search sets with pagination (20 results by default; Spring `page` and `size` parameters are supported).
- Parse al.to affiliate XML offers, normalize prices, and synchronize offers with existing sets in batches.
- Run offer synchronization on demand or automatically once per day.
- Store offers with a unique set/store pair to prevent duplicate entries.
- Browse generated API documentation in Swagger UI.

## Requirements

- Java version required by the Maven project
- Docker and Docker Compose
- A Rebrickable API key for fetching set data

## Configuration

Copy `.env.example` to `.env` and set the required values:

```dotenv
REBRICKABLE_API_KEY=your-rebrickable-api-key
DB_PASSWORD=your-local-database-password
```

`LOCAL_DB_PORT` can optionally change the host port used to access PostgreSQL (default `5432`). The application connects to `localhost:5432` by default; set `DB_URL` if you use a different connection. Spring also accepts these values as environment variables in deployed environments.

## Run locally

1. Start PostgreSQL:

   ```bash
   docker compose up -d
   ```

2. Start the application:

   ```bash
   ./mvnw spring-boot:run
   ```

On Windows, use `mvnw.cmd spring-boot:run`.

The API is available under `/api/lego-sets`. Interactive documentation is available at `/swagger-ui/index.html` while the application is running.

## Main endpoints

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/lego-sets?page=0&size=20` | List sets, paginated |
| `GET` | `/api/lego-sets/{id}` | Get a set by database ID |
| `POST` | `/api/lego-sets` | Create a set |
| `POST` | `/api/lego-sets/fetch/{setNum}` | Fetch a set from Rebrickable |
| `POST` | `/api/lego-sets/fetch-star-wars` | Import Star Wars sets from Rebrickable |
| `POST` | `/api/lego-sets/sync` | Start affiliate offer synchronization |
| `GET` | `/api/lego-sets/search?name=millennium&page=0&size=20` | Search sets by name, paginated |
| `DELETE` | `/api/lego-sets/{id}` | Delete a set |

See Swagger UI for request schemas and complete API details.
