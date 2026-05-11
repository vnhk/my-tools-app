# my-tools-app - Project Notes

> **IMPORTANT**: Keep this file updated when making significant changes to the codebase. This file serves as persistent memory between Claude Code sessions.

## Overview
Main deployable Spring Boot application that aggregates all tool modules. Pure backend — no UI framework. Frontend is React (`my-tools-react`).

## Key Architecture

### Security (`security/`)
- `SecurityConfig.java` — Spring Security config (JWT + session, CORS, form login for OTP flow)
- `JwtAuthenticationFilter.java` — validates JWT tokens on `/api/**`
- `JwtService.java` — token generation/validation
- `CustomAuthenticationProvider.java` — OTP-based authentication
- `OTPService.java` — one-time password logic
- `AuthController.java` — `POST /api/auth/login`, `POST /api/auth/otp`, `GET /api/auth/qr-*`
- `PasswordEncoderConfig.java` — BCrypt bean

### Views (`views/`)
Contains only non-UI services and controllers:
- `views/englishepub/IntegratedTextNotKnownWordsService.java` — overrides `TextNotKnownWordsService` to also filter words already known via `TranslationRecordService`
- `views/pocketapp/PocketController.java` — REST proxy for pocket browser extension
- `views/pocketapp/PocketRequest.java` — request DTO

## Important Notes
1. `my-tools-app` is integration/deployment only — business logic and REST controllers live in individual modules
2. `spring-boot-starter-parent` version: 3.2.8
3. Test infrastructure: TestContainers (MariaDB + RabbitMQ) via `ReactRunAllE2ETest`
