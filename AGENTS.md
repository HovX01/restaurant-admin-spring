# Repository Guidelines

## Project Structure & Module Organization
- Java source: `src/main/java/com/resadmin/res` (packages: `controller`, `service`, `entity`, `dto`, `config`, `security`, `util`).
- Resources: `src/main/resources` (e.g., `application.properties`, `schema.sql`, `data.sql`).
- Tests: `src/test/java/com/resadmin/res` with unit/integration tests.
- Docs and assets: `docs/`.

## Build, Test, and Development Commands
- `./gradlew clean build` (Windows: `gradlew.bat clean build`) — compile, run tests, produce JAR.
- `./gradlew test` — run JUnit 5 test suite.
- `./gradlew bootRun` — run locally. Example with profile:
  - macOS/Linux: `SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun`
  - Windows (PowerShell): `$env:SPRING_PROFILES_ACTIVE='dev'; ./gradlew.bat bootRun`

## Coding Style & Naming Conventions
- Java 17, Spring Boot 3.5.x; 4-space indent; no tabs; UTF-8.
- Packages lower-case; classes `UpperCamelCase`; methods/fields `lowerCamelCase`.
- Suffixes: `*Controller`, `*Service`, `*Repository`, `*DTO`, `*Entity`.
- Use Lombok for boilerplate (`@Getter/@Setter/@RequiredArgsConstructor`). Prefer not to use `@Data` on JPA entities.
- DTOs validated with `jakarta.validation` annotations.

## Testing Guidelines
- Framework: JUnit 5 (`useJUnitPlatform()`). Place tests under `src/test/java`.
- Naming: `ClassNameTest.java`; method names describe behavior (e.g., `shouldCreateOrder_whenValid()`).
- Write unit tests for services and Web layer, and integration tests where behavior crosses layers.
- Run: `./gradlew test`. Add tests for bug fixes and new endpoints.

## Commit & Pull Request Guidelines
- Use Conventional Commits: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:` with optional scope (e.g., `fix(security): ...`).
- PRs include: clear description, linked issues, what changed/why, tests added/updated, and any breaking changes.

## Security & Configuration Tips
- Configure via env vars or profiles: `SPRING_PROFILES_ACTIVE`, `SPRING_DATASOURCE_URL/USERNAME/PASSWORD`, `JWT_SECRET`.
- Do not commit secrets. Prefer per-environment overrides to `application.properties`.
