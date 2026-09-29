### Code Style
- Follow standard Spring Boot and Java conventions
- Use constructor injection; no field injection
- Do not create beans with `new`
- Follow existing naming conventions
- Use wildcard imports instead of separate imports for each class
- Example: `import org.springframework.web.bind.annotation.*;`
- Do not import each annotation or class one by one from the same package
- Avoid magic numbers and strings
- Never return `null` for collections
- Remove unused imports, variables, and methods
- Remove commented-out code
- Keep methods small and focused


### Architecture
- Follow Controller → Service → Repository layering
- Do not skip layers
- Do not return entities from APIs; use DTOs
- Use `@ConfigurationProperties` instead of scattered `@Value`


### Controllers
- Keep controllers thin
- Only validate input, call service, return response
- No database queries in controllers
- No business logic in controllers
- Use the standard response wrapper for success
- Use `@RestControllerAdvice` for exceptions
- Use correct HTTP methods and status codes
- Follow existing request-mapping patterns


### Services and Transactions
- Put business logic in services
- Use `@Transactional` explicitly for all DB writes
- Use `@Transactional(readOnly = true)` for reads
- Use `rollbackFor` where checked exceptions apply
- Keep transaction scope small
- No remote calls inside transactions
- Do not swallow exceptions
- Avoid self-invocation of `@Transactional` methods


### Repositories
- Reuse existing repositories
- Add methods to the nearest repository
- Use Spring Data JPA methods first
- Use native SQL only when needed
- Select only required fields
- Use projections or DTO queries for read-only data
- Use `LAZY` fetching by default
- Never build queries by string concatenation


### Validation
- Validate every API input
- Validate body, query params, and path variables
- Do not use values before validation
- Use `@Valid`, `@NotNull`, `@NotBlank`, `@Size`
- Return meaningful validation messages
- Allow legitimate empty optional values
- Return `400` for validation errors


### API Responses
- Use a consistent response structure
- Return only required data
- Do not expose internal details
- Do not expose stack traces
- Use UTC ISO-8601 for date-time fields


### Exception Handling
- Use custom business exceptions
- Map exceptions to HTTP status in one place
- No empty catch blocks
- No broad `catch (Exception)`
- Log each exception once


### Database
- Use UTC timestamps
- Use transactions for multi-table operations
- Add indexes on frequently filtered columns
- Avoid duplicate queries
- Avoid N+1 queries
- Fetch only required records
- Use Flyway/Liquibase for schema changes
- Do not use `ddl-auto=update` in production
- Use `DECIMAL` for money


### Audit Fields
- Include `createdDate`, `createdBy`
- Include `updatedDate`, `updatedBy`
- Use Spring Data JPA auditing
- Do not change `createdDate` or `createdBy` after creation
- Keep audit fields consistent on all paths


### Security
- Never trust client input
- Never bypass Spring Security
- Check resource ownership in services
- Never log passwords, tokens, keys, or secrets
- Never hardcode secrets
- Use parameterized queries only
- Hash passwords with BCrypt/Argon2
- Configure CORS explicitly
- Validate uploaded file types and sizes


### File Uploads
- Use S3 presigned URL flow
- Do not upload through the API
- Keep presigned URLs short-lived
- Generate object keys on the server
- Validate file metadata before saving
- Store only public URLs in the database
- Use presigned download URLs for private files


### Performance
- Use pagination for large datasets
- Set a maximum page size
- Avoid duplicate API and DB calls
- Avoid unnecessary joins and nested queries
- Use batch operations like `saveAll`
- Set timeouts on outbound calls
- Cache only rarely changing data


### SQL Standards
- Never use `SELECT *`
- Always specify column names
- Use meaningful aliases
- Use meaningful foreign key names
- Use `utf8mb4_unicode_ci` for new tables
- Use `snake_case` names
- Prefer stored procedures for reporting and heavy logic
- Use `ORDER BY` with pagination


### Logging
- Use SLF4J with parameterized logs
- No `System.out` or `printStackTrace`
- Use correct log levels
- Include a trace ID in logs
- Do not log sensitive data


### Testing
- Add unit tests for new logic
- Test success and failure cases
- Keep tests independent
- Pass CI and static analysis before merge


### StoreHub Standards
- Follow existing folder structure
- Follow existing controller patterns
- Follow existing repository patterns
- Follow existing endpoint patterns
- Reuse existing utilities, helpers, and services
- Stay consistent with nearby modules


### Change Scope
- Make the minimum change required
- Do not refactor unrelated code
- Do not rename methods unless required
- Do not modify working code outside scope
- Preserve existing coding patterns
- Keep imports organized
- Do not change public API contracts without approval