# my-tools-app

Main deployable Spring Boot application that aggregates all `my-tools` modules into a single runnable service. Pure backend — no UI framework. Frontend is the React app in `my-tools-react/`.

## Responsibilities

- Spring Security configuration: JWT authentication + OTP-based login
- Integration wiring for all sub-modules
- E2E test infrastructure (TestContainers: MariaDB + RabbitMQ)

## Security

| Component | Description |
|-----------|-------------|
| `SecurityConfig` | JWT + session, CORS, form login for OTP flow |
| `JwtAuthenticationFilter` | Validates JWT on all `/api/**` requests |
| `JwtService` | Token generation and validation |
| `CustomAuthenticationProvider` | OTP-based authentication |
| `OTPService` | One-time password logic |
| `AuthController` | `POST /api/auth/login`, `POST /api/auth/otp`, `GET /api/auth/qr-*` |

## Running

```bash
# Build all modules first
mvn clean install -DskipTests

# Start the app
mvn spring-boot:run -pl my-tools-app
```

## Docker

```bash
docker-compose --env-file env_my_tools up --build -d
```

## Notes

- Business logic and REST controllers live in individual modules — this module is integration/deployment only
- Spring Boot parent: `3.2.8`
- `IntegratedTextNotKnownWordsService` overrides the ebook service to also filter words already known via flashcards
- `PocketController` provides a REST proxy for the pocket browser extension
