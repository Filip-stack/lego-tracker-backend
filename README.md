# LEGO Star Wars Tracker (Backend)

> **WORK IN PROGRESS**
> This project is currently under active development. Features and endpoints are subject to change.

## About The Project

A robust RESTful API built to track, manage, and catalog a personal collection of LEGO Star Wars sets. The application integrates directly with the [Rebrickable API](https://rebrickable.com/api/) to automatically fetch accurate set details, part counts, and release years, storing them securely in a containerized relational database.

## Tech Stack

This backend service is built using modern enterprise standards and a 3-tier architecture (Controller - Service - Repository):

* **Java**
* **Spring Boot 3** (Spring Web, Spring Data JPA)
* **PostgreSQL** (Database)
* **Docker & Docker Compose** (Containerization)
* **Maven** (Build Tool)
* **Swagger / OpenAPI** (API Documentation)

## Current Features

- Containerized PostgreSQL database configured via Docker Compose.
- Automated ORM mapping with Hibernate (`LegoSet` entity).
- REST endpoints for retrieving and manually saving sets.
- `RestClient` integration for fetching live LEGO set data directly from the external Rebrickable API.
- Interactive API documentation available via Swagger UI.

## Next Steps / Upcoming Features

- [ ] Implementing Data Transfer Objects for incoming and outgoing HTTP requests.
- [ ] Exception handling (e.g., custom error messages when a set is not found on Rebrickable).
- [ ] Automated testing configuration.

---
*Created as a backend development project.*