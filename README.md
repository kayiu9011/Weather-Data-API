# Weather Data API

A full-stack weather dashboard built to learn Java and Spring Boot. The application will expose its own REST API, retrieve weather data from Open-Meteo, process forecast data, and store weather observations.

This project is being built incrementally. Milestone 1 provides the Spring Boot foundation and a health endpoint.

## Current functionality

- Spring Boot application running on an embedded web server
- `GET /api/health` endpoint
- Automated application-context and controller tests

## Technology stack

- Java 21
- Spring Boot 3.5
- Maven
- JUnit 5

## Run locally

Requirements: Java 21 and Maven 3.6.3 or newer.

```bash
mvn spring-boot:run
```

Then open <http://localhost:8080/api/health>. The response should be:

```json
{
  "status": "UP"
}
```

Run the tests with:

```bash
mvn test
```

## Planned milestones

1. Spring Boot foundation
2. Open-Meteo integration
3. Weather-data processing
4. REST API endpoints
5. H2 and JPA persistence
6. HTML, CSS, and JavaScript frontend
7. Error handling and tests
8. Portfolio polish
