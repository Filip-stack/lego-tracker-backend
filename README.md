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

- Java 25
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

The API is available under `http://localhost:8080/api/lego-sets`. Interactive documentation is available at `http://localhost:8080/swagger-ui/index.html` while the application is running.

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

## Example requests

Fetch a set from Rebrickable by its set number:

```bash
curl -X POST http://localhost:8080/api/lego-sets/fetch/75192
```

Create a set manually:

```bash
curl -X POST http://localhost:8080/api/lego-sets \
  -H "Content-Type: application/json" \
  -d '{
    "setNum": "75192-1",
    "name": "Millennium Falcon",
    "releaseYear": 2017,
    "themeId": 158,
    "numParts": 7541,
    "setImgUrl": "https://example.com/75192.jpg"
  }'
```

Search sets and request a later page:

```bash
curl "http://localhost:8080/api/lego-sets/search?name=Falcon&page=0&size=10"
```

Trigger an offer synchronization:

```bash
curl -X POST http://localhost:8080/api/lego-sets/sync
```

Paginated endpoints return Spring Data's `Page` response, including `content`, `totalElements`, `totalPages`, `number`, and `size`. Page numbering starts at `0`.

## Data sources and synchronization

Set details and the Star Wars catalog are fetched from Rebrickable. The Rebrickable API key is required for these operations. Offer synchronization runs daily and can also be started with `POST /api/lego-sets/sync`. The current al.to parser reads the bundled sample file `src/main/resources/alto_feed.xml`; configure a live feed source before relying on it for current store prices.

## Error responses

Validation errors, duplicate set numbers, and missing sets return an appropriate HTTP status with a JSON body, for example:

```json
{
  "error": "Zestaw juz istnieje",
  "message": "...",
  "status": "409"
}
```

## Database

Hibernate updates the schema automatically on startup (`spring.jpa.hibernate.ddl-auto=update`). For persistent deployments, use a managed migration tool and back up the database before schema changes.
