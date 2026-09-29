### Code Style
- Follow standard Spring Boot and Java coding conventions
- Use dependency injection instead of manual object creation where applicable
- Prefer constructor injection
- Follow existing naming conventions
- Remove unused imports, variables, and methods
- Remove commented-out code before final output


### Controllers
- Keep controllers thin
- Controllers should only validate input, call service methods, and return responses
- Do not write database queries inside controllers
- Do not write business logic inside controllers
- Always use the project's standard response wrapper for successful responses
- Use centralized exception handling with `@ControllerAdvice` / `@ExceptionHandler` for failures


### Repositories
- Reuse existing repository interfaces whenever possible
- Add methods to the nearest existing repository
- Use Spring Data JPA repository methods before considering native SQL
- Use native SQL only when performance or complexity requires it
- Always select only the required fields for read-only queries
- Use projections or DTO-based queries when only specific fields are required
- Fetch only required columns


### Validation
- Every API input must be validated
- Validate request body, query parameters, and path variables as applicable
- Do not use request values before validation
- Use Bean Validation annotations such as `@Valid`, `@NotNull`, `@NotBlank`, and `@Size` as applicable
- Return meaningful validation messages
- Handle nullable optional fields according to the API contract and validation rules; do not reject legitimate empty optional values


### API Responses
- Return consistent response structures
- Return only the data required by the client
- Do not expose internal implementation details
- Do not expose stack traces in API responses


### Database
- Use UTC timestamps
- Use transactions for multi-table operations
- Add indexes for frequently filtered columns
- Avoid duplicate database queries
- Avoid N+1 query patterns
- Fetch only required records


### Audit Fields
- Include createdDate where applicable
- Include createdBy where applicable
- Include updatedDate where applicable
- Include updatedBy where applicable
- Maintain audit fields consistently


### Security
- Never trust client input
- Never bypass Spring Security authentication and authorization
- Never log passwords, tokens, access keys, or secrets
- Use parameterized queries or safe JPA query mechanisms only
- Validate uploaded file types and sizes


### File Uploads
- Use S3 presigned URL flow for uploads
- Never upload files through the API when presigned upload is available
- Store only public URLs in the database
- Validate file metadata before saving


### Performance
- Avoid duplicate API calls
- Avoid duplicate database calls
- Use pagination for large datasets
- Avoid unnecessary joins
- Avoid unnecessary nested queries
- Use batch operations such as `saveAll` where appropriate
- Keep transaction scope as small as possible


### SQL Standards
- Never use SELECT *
- Always specify column names
- Use meaningful aliases
- Use meaningful foreign key names
- Use utf8mb4_unicode_ci for new tables
- Prefer stored procedures for reporting and heavy business logic


### StoreHub Standards
- Follow existing folder structure
- Follow existing controller patterns
- Follow existing repository patterns
- Follow existing API endpoint and request-mapping patterns
- Reuse existing utilities, helpers, services, and components before creating new ones
- Maintain consistency with nearby modules


### Change Scope
- Make the minimum change required
- Do not refactor unrelated code
- Do not rename existing methods unless required
- Do not modify working functionality outside the requested scope
- Preserve existing coding patterns when extending features
- Keep imports organized and concise
- Always use an explicit transaction pattern with `@Transactional` and appropriate exception handling for all database transactions
