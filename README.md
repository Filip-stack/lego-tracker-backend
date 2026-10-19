# LEGO Star Wars Tracker (Backend)

> **WORK IN PROGRESS**
> This project is currently under active development. Features and endpoints are subject to change.

## About The Project

A robust RESTful API built to track, manage, and catalog a collection of LEGO Star Wars sets, enriched with a real-time price comparison engine. The application integrates directly with the [Rebrickable API](https://rebrickable.com/api/) to fetch accurate set details, and consumes external affiliate network XML feeds to automatically track current store prices. All data is securely stored in a containerized relational database.

## Tech Stack

This backend service is built using modern enterprise standards and a 3-tier architecture (Controller - Service - Repository):

* **Java**
* **Spring Boot 4.1.1** (Spring Web, Spring Data JPA, Scheduling)
* **PostgreSQL** (Database)
* **Docker & Docker Compose** (Containerization)
* **Jackson XML/JSON** (Data parsing and serialization)
* **Maven** (Build Tool)
* **Swagger / OpenAPI** (API Documentation)

## Current Features

- Containerized PostgreSQL database configured via Docker Compose.
- Automated ORM mapping with Hibernate (`LegoSet` and `StoreOffer` entities with relational mapping).
- REST endpoints for retrieving, searching, and manually saving sets.
- `RestClient` integration for fetching live LEGO set data directly from the external Rebrickable API.
- Automated background jobs (`@Scheduled`) for synchronizing store prices from XML affiliate feeds.
- Robust database UPSERT mechanism protecting against duplicate offer records.
- Interactive API documentation available via Swagger UI.

## Getting Started

1. Copy `.env.example` to `.env` and fill in your values (the file is git-ignored):
   - `REBRICKABLE_API_KEY` – get one at [rebrickable.com](https://rebrickable.com/api/) (Account → Settings → API),
   - `DB_PASSWORD` – any password for the local PostgreSQL.
2. Start the database: `docker compose up -d`
3. Run the app: `./mvnw spring-boot:run`

Both Spring Boot and Docker Compose read `.env` automatically. In production, set the same names as regular environment variables instead.

## Next Steps / Upcoming Features

- [x] Implementing Data Transfer Objects (DTO) for incoming XML feeds and HTTP requests.
- [ ] Global exception handling (e.g., custom error messages when a set is not found).
- [ ] Automated testing configuration (JUnit/Mockito).

---
*Created as a backend development project.*