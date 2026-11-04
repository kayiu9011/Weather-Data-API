# Weather Data API

A full-stack weather dashboard built with Java and Spring Boot. Search for a city to see current conditions, a 12-hour forecast, calculated weather insights, a rain recommendation, and previously saved observations.

The browser never contacts the external provider directly. It calls this application's REST API, which retrieves and transforms data from [Open-Meteo](https://open-meteo.com/).

## Features

- Search cities worldwide using Open-Meteo geocoding
- View temperature, humidity, wind speed, condition, and observation time
- Browse the next 12 forecast hours
- Calculate average forecast temperature in Java
- Find the hottest upcoming hour in Java
- Recommend an umbrella when rain probability reaches 50%
- Save current observations to an H2 database
- Retrieve recent city history with a configurable limit
- Return consistent JSON errors for invalid input and service failures
- Use a responsive frontend built without a CSS or JavaScript framework

## Architecture

```text
┌────────────────────┐
│ HTML / CSS / JS    │
└─────────┬──────────┘
          │ HTTP + JSON
          ▼
┌────────────────────┐
│ Spring Controllers │
└─────────┬──────────┘
          │
          ▼
┌────────────────────┐
│ Weather Service    │
│                    │
│ • averages         │
│ • hottest hour     │
│ • rain advisory    │
│ • DTO mapping      │
└────────┬────────┬──┘
         │        │
         ▼        ▼
   Open-Meteo    JPA
                  │
                  ▼
                 H2
```

The package responsibilities are intentionally simple:

- `controller` handles HTTP routes and converts exceptions into API errors.
- `service` coordinates requests, performs calculations, and controls persistence.
- `client` contains Open-Meteo URLs and HTTP calls.
- `repository` provides database operations through Spring Data JPA.
- `model` contains the persisted weather entity.
- `dto` contains the data shapes exchanged between layers and returned as JSON.

## Technology stack

- Java 21
- Spring Boot 3.5
- Spring Web and `RestClient`
- Spring Data JPA
- H2 Database
- Maven
- JUnit 5, AssertJ, Mockito, and MockMvc
- HTML5, responsive CSS, and vanilla JavaScript

## How it works

1. The frontend sends a city name to the Spring Boot REST API.
2. `WeatherApiClient` asks Open-Meteo's geocoding API for coordinates.
3. The client requests current or hourly weather using those coordinates.
4. Jackson maps the external JSON into Java records.
5. `WeatherService` transforms external DTOs into application DTOs.
6. Java streams calculate averages, maximum temperatures, and rain risk.
7. Current observations are saved through JPA.
8. Spring serializes the application DTOs as JSON for the browser.

## REST API

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/health` | Application health check |
| `GET` | `/api/weather/current?city=Singapore` | Current weather; also saves an observation |
| `GET` | `/api/weather/forecast?city=Singapore` | Next 12 hourly forecast entries |
| `GET` | `/api/weather/average?city=Singapore` | Calculated mean forecast temperature |
| `GET` | `/api/weather/hottest?city=Singapore` | Hottest forecast hour |
| `GET` | `/api/weather/umbrella?city=Singapore` | Rain-based umbrella recommendation |
| `GET` | `/api/weather/history?city=Singapore` | Ten newest saved observations |
| `GET` | `/api/weather/history?city=Singapore&limit=20` | Newest observations with a custom limit |

The history limit must be between 1 and 100.

## Example responses

Current weather:

```json
{
  "city": "Singapore",
  "temperature": 30.2,
  "humidity": 74,
  "windSpeed": 8.4,
  "condition": "Partly Cloudy",
  "observedAt": "2026-11-04T12:00:00"
}
```

Hourly forecast:

```json
{
  "city": "Singapore",
  "hourly": [
    {
      "time": "2026-11-04T13:00:00",
      "temperature": 31.4,
      "rainProbability": 20,
      "condition": "Partly Cloudy"
    }
  ]
}
```

Umbrella recommendation:

```json
{
  "city": "Singapore",
  "umbrellaRecommended": true,
  "message": "Rain is expected in the next 12 hours. Bring an umbrella!"
}
```

Error response:

```json
{
  "timestamp": "2026-11-04T12:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "City not found: UnknownCity",
  "path": "/api/weather/current"
}
```

## Run locally

Requirements:

- Java 21
- Maven 3.6.3 or newer

Clone the repository, enter its directory, and start the application:

```bash
mvn spring-boot:run
```

Open <http://localhost:8080> for the dashboard or <http://localhost:8080/api/health> for the health check.

The development database is created under `data/`. Its files are ignored by Git. The optional H2 console is available at <http://localhost:8080/h2-console> with:

```text
JDBC URL: jdbc:h2:file:./data/weatherdb
User: sa
Password: (leave blank)
```

Run the automated tests:

```bash
mvn test
```

Create an executable JAR:

```bash
mvn clean package
java -jar target/weather-data-api-0.0.1-SNAPSHOT.jar
```

## Screenshots

Add a dashboard screenshot here before publishing the repository:

```text
docs/screenshots/weather-dashboard.png
```

Suggested Markdown after adding the image:

```markdown
![Weather Data API dashboard](docs/screenshots/weather-dashboard.png)
```

## What I learned

- How Spring Boot starts and configures an embedded web application
- How controllers, services, clients, repositories, entities, and DTOs differ
- How constructor injection connects Spring-managed components
- How Jackson maps external JSON into Java records
- How to process collections with streams and comparators
- How JPA maps a Java entity to a relational database table
- How a browser calls a backend REST API using `fetch()`
- How to test service logic without calling a real external API
- How controller advice produces predictable HTTP errors

## Future improvements

- Migrate H2 to PostgreSQL for deployment
- Cache repeated city and forecast requests
- Add daily forecasts and unit selection
- Add database migrations with Flyway
- Add integration tests with a mock HTTP server
- Containerize the application with Docker
- Deploy the backend and database to a cloud platform
- Add charts for temperature and rain probability trends

## Data attribution

Weather forecasts are provided by [Open-Meteo](https://open-meteo.com/). Location search data is based on GeoNames through Open-Meteo's geocoding API.
