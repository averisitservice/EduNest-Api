# EduNest Backend

Spring Boot REST API powering EduNest — a multi-tenant school/institute management system covering students, teachers, classes, attendance, exams, fees, homework, announcements, events, and timetables. Serves the React admin panel (`EduNest-Web`, teachers) and the Flutter mobile app (`EduNest-App`, students).

## Tech Stack

- **Java 21**, **Spring Boot 3.4.8**
- **Spring Web** — REST controllers
- **Spring Data JPA** (Hibernate) — persistence
- **PostgreSQL** — primary database (MySQL connector also on the classpath)
- **Spring Security** — stateless auth via a custom JWT filter
- **JJWT 0.12.6** — JWT issuing/parsing
- **Razorpay Java SDK** — payment order creation/verification (`RazorpayConfiguration`, exposed via `MobileFeeController`)
- **Cloudinary Java SDK** / **AWS SDK v2 (S3)** — file storage for homework/note attachments, switched via the `is-live` property (`CloudinaryConfiguration`, `AwsConfiguration`, unified behind `FileStorageService`)
- **Lombok** — boilerplate reduction
- **Gradle** — build tool

## Project Structure

```
src/main/java/com/edunest/
├── EdunestApplication.java     # Spring Boot entry point
├── common/                     # Shared response wrapper (ResponseObject)
├── configuration/              # JWT filter/helper, Spring Security config, third-party client config
│                                #   (Razorpay, Cloudinary, AWS S3 — each holds its client @Bean plus
│                                #   the business/CRUD methods directly, no separate service interface)
├── constant/                   # App-wide constants
├── controller/                 # REST controllers (auth, student, teacher, class, timetable, lookup,
│                                #   announcement, attendance, dashboard, event, exam, fee, homework,
│                                #   mobile auth/student/school)
├── dto/                        # Request/response DTOs, grouped by feature
├── entity/                     # JPA entities
├── error/                      # Custom exception + global exception handler
├── helper/                     # Utility helpers (CryptoHelper, CommonHelper)
├── repository/                 # Spring Data JPA repositories
└── service/                    # Service interfaces + implementations (FileStorageService switches
                                 #   between Cloudinary/AWS S3 based on `is-live`)

src/main/resources/
├── application.properties      # Runtime configuration
└── templates/email/            # HTML email templates (password reset, student password reset)
```

The app is multi-tenant: most authenticated endpoints derive a `tenantId` (and often the acting `teacherId`) from claims embedded in the JWT via `JwtHelper`, rather than from request parameters.

## Prerequisites

- JDK 21
- PostgreSQL instance with a database named `EduNest`
- (Optional) Gmail account with an app password if you need email sending to work
- (Optional) Razorpay `key_id` / `key_secret` if you plan to wire up `RazorpayConfiguration`
- (Optional) Cloudinary credentials (`cloud-name` / `api-key` / `api-secret`) for file uploads when `is-live=false`
- (Optional) AWS S3 credentials + bucket for file uploads when `is-live=true`

## Configuration

Runtime config lives in `src/main/resources/application.properties`. Key properties:

| Property | Purpose |
|---|---|
| `server.port` | HTTP port (default `8081`) |
| `spring.datasource.url` / `username` / `password` | PostgreSQL connection |
| `spring.jpa.hibernate.ddl-auto` | Schema management (`update`) |
| `spring.mail.*` | SMTP settings for outgoing email |
| `security.jwt.secret-key` | JWT signing key |
| `security.jwt.expiration-time` | Access token TTL (ms) |
| `security.jwt.refresh-expiration-time` | Refresh/session TTL (s) |
| `APP_KEY` / `APP_IV` | Symmetric encryption key/IV used by `CryptoHelper` |
| `razorpay.key-id` / `razorpay.key-secret` | Razorpay API credentials used by `RazorpayConfiguration` |
| `is-live` | File storage switch: `true` uploads attachments to AWS S3, `false` uploads to Cloudinary |
| `cloudinary.cloud-name` / `cloudinary.api-key` / `cloudinary.api-secret` | Cloudinary credentials used by `CloudinaryConfiguration` |
| `aws.access-key` / `aws.secret-key` / `aws.region` / `aws.s3.bucket-name` | AWS S3 credentials used by `AwsConfiguration` |

> **Security note:** `application.properties` currently contains real credentials and is tracked by git (not in `.gitignore`). Move these to environment variables or a local, git-ignored properties file before pushing/sharing the repo.

## Running Locally

```bash
# Unix/macOS
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

The API will be available at `http://localhost:8081`.

Run tests:

```bash
./gradlew test
```

Build a jar:

```bash
./gradlew build
```

Fast compile check (no test run):

```bash
./gradlew compileJava
```

## Authentication

- `POST /auth/login` and `/lookup/role` (via `lookup/role`) are the only public endpoints; everything else requires a valid JWT.
- Send the token as `Authorization: Bearer <token>` on every subsequent request.
- `JwtAuthenticationFilter` validates the token per-request; controllers pull `tenantId` / `teacherId` out of it via `JwtHelper`.
- `POST /auth/renew-session` exchanges a refresh token for a new session.
- Mobile (student) auth is separate, under `/api/...` (`MobileAuthController`).

## API Overview

All responses are wrapped in a common `ResponseObject<T>` (`{ success, data, ... }`).

### Auth (`/auth`)
| Method | Path | Description |
|---|---|---|
| POST | `/auth/login` | Authenticate and receive JWT + refresh token |
| POST | `/auth/renew-session` | Renew an expired session |

### Students (`/student`)
| Method | Path | Description |
|---|---|---|
| GET | `/student/list` | List students for the current tenant |
| GET | `/student/{studentId}` | Get a student by ID |
| POST | `/student` | Create/update a student |
| DELETE | `/student/{studentId}` | Delete a student |

### Teachers (`/teacher`)
| Method | Path | Description |
|---|---|---|
| GET | `/teacher/list` | List teachers for the current tenant |
| GET | `/teacher/{teacherId}` | Get a teacher by ID |
| POST | `/teacher` | Create/update a teacher |
| DELETE | `/teacher/{teacherId}` | Delete a teacher |

### Classes (`/class`)
| Method | Path | Description |
|---|---|---|
| GET | `/class/list` | List classes for the current tenant |
| GET | `/class/{classId}` | Get a class by ID |
| POST | `/class` | Create/update a class |
| DELETE | `/class/{classId}` | Delete a class |

### Timetable (`/timetable`)
| Method | Path | Description |
|---|---|---|
| GET / POST | `/timetable/working-days` | Get/save the tenant's working days |
| GET / POST | `/timetable/time-slots` / `/timetable/time-slots/{classId}` | Save time slots / list time slots for a class |
| GET | `/timetable/{classId}/{sectionId}` | Get the timetable for a class section |
| POST | `/timetable/cell` | Save a single timetable cell (subject/teacher/slot assignment) |
| GET | `/timetable/teacher/{teacherId}` | Get a teacher's personal timetable |

### Lookup (`/lookup`)
| Method | Path | Description |
|---|---|---|
| GET | `/lookup/roles` | All roles |
| GET | `/lookup/employmentTypes` | All employment types |
| GET | `/lookup/subject` | Subjects for the current tenant |
| GET | `/lookup/classMaster` | Class masters for the current tenant |
| GET | `/lookup/classSection` | Class masters with their sections |
| POST | `/lookup/subject/save` | Create/update a subject |

### Other modules

`AnnouncementController`, `AttendanceController`, `DashboardController`, `EventController`, `ExamController`, `FeeController`, and `HomeworkController` follow the same list/get/save/delete pattern scoped by `tenantId`. `MobileAuthController` and `MobileStudentController` expose the equivalent read-only/self-service views for the student mobile app under `/api/...` (student login/password, home/profile, timetable, exams, homework, notes — `GET /api/student/homework` and `GET /api/student/notes` both accept optional `fromDate`/`toDate` query params for date-range filtering).

`HomeworkController` (`POST /homework`) and `NoteController` (`POST /note`) accept `multipart/form-data`: a `data` part with the JSON request body and an optional `file` part for the attachment. When a file is present, `HomeworkServiceImpl`/`NoteServiceImpl` upload it via `FileStorageService` (Cloudinary or AWS S3, per `is-live`) and store the resulting URL as `attachmentUrl`.

`MobileFeeController` (`/api/student/fee`) handles the mobile fee-payment flow: `GET /detail` (pending/paid summary), `POST /create-order` (creates a Razorpay order for the pending or a partial amount), `POST /verify-payment` (verifies the Razorpay signature and records the payment). Business logic for order creation/verification lives in `FeeService`, which delegates the Razorpay-specific parts to `RazorpayConfiguration`.

## Domain Model (key entities)

`Tenant`, `Role`, `Teacher`, `Student`, `ClassMaster`, `ClassSection`, `ClassSubject`, `ClassFee`, `Subject`, `TeacherClass`, `TeacherSubject`, `StudentClass`, `AcademicYear`, `EmploymentType`, `WorkingDay`, `TimeSlot`, `Timetable`, `Announcement`, `Attendance`, `Event`, `Exam`, `ExamMark`, `ExamSchedule`, `Homework`, `Note`, `FeePayment`, `RazorpayOrder`, `RazorpayTransaction`, `PaymentWebhookLog`.

## Error Handling

`CustomException` + `CustomExceptionHandler` provide centralized error responses; validation/business errors are surfaced as structured `ErrorItem`s within the standard `ResponseObject` envelope.

## Development Guidelines

These conventions apply to all new and edited code in this repository.

### Code style rules

- **Do NOT write comments in Java code.** No line comments (`//`), block comments (`/* */`), or Javadoc (`/** */`). Keep method and variable names descriptive enough that the code explains itself.
- **Do NOT use `@Builder` (or `@Builder.Default`) on entities or DTOs.** Use `@Getter/@Setter/@NoArgsConstructor/@AllArgsConstructor` and plain field initializers; construct objects with `new` + setters.
- **Variable names must be descriptive, full words** — no cryptic abbreviations (`sc`, `cs`, `wd`, `e`, `a`, `m`). Match the entity/DTO type name, camelCased: an `Exam` variable is `exam`, a `ClassSection` variable is `classSection`, an `ExamScheduleRequest` variable is `examScheduleRequest`.
- **`for`-each loop variables follow the same rule**: name the loop variable as the singular of the collection it iterates, matching its element type — not a single letter or abbreviation.
  ```java
  // Do this
  for (Student student : students) { ... }
  for (ClassSection classSection : classSections) { ... }
  for (ExamScheduleRequest examScheduleRequest : subjects) { ... }

  // Not this
  for (Student s : students) { ... }
  for (ClassSection cs : classSections) { ... }
  ```
  The one exception is a short-lived stream/lambda parameter used only inline (e.g. `.filter(a -> a.getStatus().equals("P"))`) where the surrounding context makes the type obvious.
- **DTO naming depends on whether the shape is one-way or shared.** If a DTO is only ever sent to the server, name it `XxxRequest`; if it's only ever sent back, name it `XxxResponse`. If the same class is used as *both* the save-request body and the get-by-id response (a common shortcut when create/read share a shape), name it `XxxDTO` instead — `XxxRequest` returned from a GET endpoint reads backwards. Example: `Student` save/get both use `StudentDTO`, not `StudentRequest`.

### Layout & conventions

- Layers: `controller/` → `service/` (interface + `Impl`) → `repository/` → `entity/`; DTOs in `dto/<module>/`.
- Every response is wrapped in `ResponseObject<T>` (`{success, errors, data}`).
- Errors: throw `new CustomException("<param>", "<message>")` → HTTP 400 with `errors[0].msg`.
- Multi-tenant: extract `tenantId` from the JWT and scope every query by it.
- **Mobile (student) API lives under `/api/...`** (`MobileAuthController`, `MobileStudentController`, `MobileSchoolController`); web endpoints are unprefixed and authenticate Teachers.
- `ddl-auto=update`, no migrations — new entity columns must be nullable.
- Shared, genuinely-duplicated helper logic (e.g. `getCurrentYear`, `teacherName`, `studentName`, `rollNo`, `fullAddress`, `generateRandomPassword`, `generateAdmissionNo`, `generateUsername`, `subjectName`) lives in `helper/CommonHelper`; don't reintroduce per-service copies of logic that already exists there.
