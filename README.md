# SnailMailSender

_A component within the messaging ecosystem responsible for the transmission of physical letters. This module uploads letters to a SAMBA share, facilitating their processing by office services._

## Getting Started

### Prerequisites

- **Java 21 or higher**
- **Maven**
- **MariaDB**
- **Git**
- **[Dependent Microservices](#dependencies)**
- **[Samba share](#dependencies)**

### Installation

1. **Clone the repository:**

   ```bash
   git clone git@github.com:Sundsvallskommun/api-service-snailmail-sender.git
   ```

2. **Configure the application:**

   Before running the application, you need to set up configuration settings.
   See [Configuration](#Configuration)

   **Note:** Ensure all required configurations are set; otherwise, the application may fail to start.

3. **Ensure dependent services are running:**

   If this microservice depends on other services, make sure they are up and accessible. See [Dependencies](#dependencies) for more details.

4. **Build and run the application:**

     ```bash
     mvn spring-boot:run
     ```

## Dependencies

This microservice depends on the following services:

- **Citizen**

  - **Purpose:** To find address information about a citizen.
  - **Repository:** Not available at this moment.
  - **Additional Notes:** Citizen is a API serving data from [Metadatakatalogen](https://utveckling.sundsvall.se/digital-infrastruktur/metakatalogen). 

- **Samba Share**
  - **Purpose:** For uploading the files to ensure they are processed by office services.
  - **Setup Instructions:** 
    1. Ensure you have a SAMBA share set up.
    2. Refer to the [Configuration](#configuration) section for detailed instructions on how to configure the service to use the SAMBA share.


Ensure that these services are running and properly configured before starting this microservice.

## API Documentation

Access the API documentation via Swagger UI:

- **Swagger UI:** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

Alternatively, refer to the `openapi.yml` file located in the project's resource directory for the OpenAPI specification.

## Usage

### API Endpoints

Refer to the [API Documentation](#api-documentation) for detailed information on available endpoints.

### Example Request

```bash
curl -X GET http://localhost:8080/api/resource
```

## Configuration

Configuration is crucial for the application to run successfully. Ensure all necessary settings are configured in `application.yml`.

### Key Configuration Parameters

- **Server Port:**

  ```yaml
  server:
    port: 8080
  ```
- **Database Settings:**

  ```yaml
  spring:
    datasource:
      url: jdbc:mariadb://localhost:3306/your_database
      username: your_db_username
      password: your_db_password
  ```

- **Citizen configuration**

  ```yaml
  integration:
    citizen:
      base-url: https://service_url

  security:
    oauth2:
      client:
        provider:
          citizen:
            token-uri: https://token_url

        registration:
          citizen:
            authorization-grant-type: client_credentials
            provider: citizen
            client-id: the-client-id
            client-secret: the-client-secret

  ```
- **SAMBA configuration:**
  ```yaml
  integration:
    samba:
      host: localhost
      port: 445
      domain: WORKGROUP
      username: your_user
      password: your_password
      share: /path/to/your/share/
      connect-timeout: PT5S
      response-timeout: PT10S
  ```

### Database Initialization

The project is set up with [Flyway](https://github.com/flyway/flyway) for database migrations. Flyway is disabled by default so you will have to enable it to automatically populate the database schema upon application startup.

```yaml
spring:
  flyway:
    enabled: true
```

- **No additional setup is required** for database initialization, as long as the database connection settings are correctly configured.
### Additional Notes

- **Application Profiles:**

  Use Spring profiles (`dev`, `prod`, etc.) to manage different configurations for different environments.

- **Logging Configuration:**

  Adjust logging levels if necessary.

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](https://github.com/Sundsvallskommun/.github/blob/main/.github/CONTRIBUTING.md) for guidelines.

## License

This project is licensed under the [MIT License](LICENSE).

## Code status

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-snailmail-sender&metric=alert_status)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-snailmail-sender)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-snailmail-sender&metric=reliability_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-snailmail-sender)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-snailmail-sender&metric=security_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-snailmail-sender)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-snailmail-sender&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-snailmail-sender)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-snailmail-sender&metric=vulnerabilities)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-snailmail-sender)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-snailmail-sender&metric=bugs)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-snailmail-sender)

---

© 2024 Sundsvalls kommun
