# EduNest-backend

Spring Boot REST API (Java 21, PostgreSQL) for the EduNest school ERP. Serves the
React admin panel (`EduNest-Web`, teachers) and the Flutter mobile app
(`EduNest-App`, students).

## Code style rules

- **Do NOT write comments in Java code.** No line comments (`//`), block comments
  (`/* */`), or Javadoc (`/** */`). Keep method and variable names descriptive
  enough that the code explains itself. This applies to all new and edited files.
- **Do NOT use `@Builder` (or `@Builder.Default`) on entities or DTOs.** Use
  `@Getter/@Setter/@NoArgsConstructor/@AllArgsConstructor` and plain field
  initializers; construct objects with `new` + setters.

## Commands

```bash
./gradlew.bat compileJava     # fast compile check (Windows)
./gradlew.bat bootRun         # run the API (port 8081)
./gradlew.bat build           # full build + tests
```

Java toolchain 21, Spring Boot 3.4.8.

## Layout & conventions

- Layers: `controller/` → `service/` (interface + `Impl`) → `repository/` → `entity/`; DTOs in `dto/<module>/`.
- Every response is wrapped in `ResponseObject<T>` (`{success, errors, data}`).
- Errors: throw `new CustomException("<param>", "<message>")` → HTTP 400 with `errors[0].msg`.
- Multi-tenant: extract `tenantId` from the JWT and scope every query by it.
- **Mobile (student) API lives under `/api/...`** (`MobileAuthController`, `MobileStudentController`,
  `MobileSchoolController`); web endpoints are unprefixed and authenticate Teachers.
- `ddl-auto=update`, no migrations — new entity columns must be nullable.
