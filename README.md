# EduNest Backend

Spring Boot REST API powering EduNest — a multi-tenant school/institute management system covering students, teachers, classes, attendance, exams, fees, homework, notes, announcements, leave requests, timetables, and push notifications. Serves the React admin panel (`EduNest-Web`, teachers) and the Flutter mobile app (`EduNest-App`, students).

## Tech Stack

| Component | Technology |
|---|---|
| Language / runtime | Java 21 |
| Framework | Spring Boot 3.4.8 (Spring Web, Spring Data JPA, Spring Security) |
| Build tool | Gradle (wrapper bundled, Gradle 9.5.1) |
| Database | PostgreSQL (`org.postgresql:postgresql`) — MySQL connector (`com.mysql:mysql-connector-j`) is also on the classpath but unused by current config |
| Auth | Stateless JWT via a custom filter — **JJWT 0.12.6** (`jjwt-api`/`jjwt-impl`/`jjwt-jackson`) |
| Payments | Razorpay Java SDK 1.4.4 — order creation/verification (`RazorpayConfiguration`, exposed via `MobileFeeController`) |
| File storage | Cloudinary HTTP5 SDK 2.4.0 **or** AWS SDK v2 S3 2.29.52 — switched via the `is-live` property (`CloudinaryConfiguration`, `AwsConfiguration`) |
| Push notifications | Firebase Admin SDK 9.4.1 (`FirebaseConfig`) — disabled gracefully when no credentials file is configured |
| Email | Spring Mail (SMTP) |
| Misc | Lombok (boilerplate), Apache Commons Text 1.12.0 (`CommonHelper.generateRandomPassword`) |

There is **no OpenAPI/Swagger setup, no Postman collection, no Dockerfile, and no CI workflow** in this repository — none of that tooling exists here.

## Project Structure

```
src/main/java/com/edunest/
├── EdunestApplication.java     # Spring Boot entry point
├── common/                     # Shared response wrappers (ResponseObject, PagedResponse)
├── configuration/              # JWT filter/helper, Spring Security config, third-party client config
│                                #   (Razorpay, Cloudinary, AWS S3, Firebase — each holds its client @Bean
│                                #   plus the business/CRUD methods directly, no separate service interface)
├── constant/                   # App-wide constants (Constant.java) — status/type codes, roles
├── controller/                 # REST controllers — see API Overview below
├── dto/                        # Request/response DTOs, grouped by feature
├── entity/                     # JPA entities
├── error/                      # CustomException + CustomExceptionHandler (@ControllerAdvice)
├── helper/                     # Utility helpers (CommonHelper, CryptoHelper)
├── repository/                 # Spring Data JPA repositories
├── scheduler/                  # @Scheduled background jobs (announcement publishing, birthday pushes)
└── service/                    # Service interfaces + implementations (FileStorageService switches
                                 #   between Cloudinary/AWS S3 based on `is-live`)

src/main/resources/
├── application.properties      # Runtime configuration (contains real secrets — see Security note below)
└── templates/email/            # HTML email templates (password reset, student password reset)

src/test/java/com/edunest/
└── EdunestApplicationTests.java  # Default Spring context-load smoke test — the only test in the project
```

The app is multi-tenant: most authenticated endpoints derive a `tenantId` (and often the acting `teacherId`/`studentId`) from claims embedded in the JWT via `JwtHelper`, rather than from request parameters.

## Prerequisites

- JDK 21
- PostgreSQL instance with a database named `EduNest`
- (Optional) Gmail account with an app password if you need email sending to work
- (Optional) Razorpay `key_id` / `key_secret` if you plan to wire up `RazorpayConfiguration`
- (Optional) Cloudinary credentials (`cloud-name` / `api-key` / `api-secret`) for file uploads when `is-live=false`
- (Optional) AWS S3 credentials + bucket for file uploads when `is-live=true`
- (Optional) Firebase service account JSON on the classpath if you need push notifications to work

## Configuration

Runtime config lives in `src/main/resources/application.properties`. Key properties:

| Property | Purpose |
|---|---|
| `spring.application.name` | Application name |
| `server.port` | HTTP port (default `8081`) |
| `is-live` | File storage switch: `true` uploads attachments to AWS S3, `false` uploads to Cloudinary |
| `spring.datasource.url` / `username` / `password` | PostgreSQL connection |
| `spring.jpa.hibernate.ddl-auto` | Schema management (`update` — see note below) |
| `spring.jpa.database-platform` | Hibernate dialect |
| `spring.mail.*` | SMTP host/port/username/password + auth/starttls flags for outgoing email |
| `security.jwt.secret-key` | JWT signing key (HS512) |
| `security.jwt.expiration-time` | Teacher access token TTL (ms) |
| `security.jwt.refresh-expiration-time` | Teacher refresh/session TTL (s) |
| `security.jwt.student-expiration-time` | Student (mobile) access/refresh token TTL (ms) |
| `APP_KEY` / `APP_IV` | Symmetric AES key/IV used by `CryptoHelper.encrypt`/`decrypt` |
| `razorpay.key-id` / `razorpay.key-secret` | Razorpay API credentials used by `RazorpayConfiguration` |
| `cloudinary.cloud-name` / `cloudinary.api-key` / `cloudinary.api-secret` | Cloudinary credentials used by `CloudinaryConfiguration` |
| `aws.access-key` / `aws.secret-key` / `aws.region` / `aws.s3.bucket-name` | AWS S3 credentials used by `AwsConfiguration` |
| `firebase.credentials-file` | Classpath path to the Firebase service account JSON used by `FirebaseConfig`; if unset, push notifications are silently disabled (`FirebaseConfig.isEnabled()` returns `false`) |

> **Security note:** `application.properties` currently contains real credentials (DB password, mail app password, JWT secret, Razorpay keys, Cloudinary keys) and is **not** in `.gitignore` — only `src/main/resources/firebase-service-account.json` is gitignored. Move these to environment variables or a local, git-ignored properties file before pushing/sharing the repo.

## Running Locally

```bash
# Unix/macOS
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

The API will be available at `http://localhost:8081`.

Run tests (only the default Spring context-load smoke test exists — there is no other test coverage in this project):

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

Two independent JWT schemes exist, both HS512-signed and issued by `JwtHelper`, sharing `security.jwt.secret-key`:

- **Teacher (web admin panel)** — unprefixed endpoints (e.g. `/student`, `/teacher`, `/exam`). `POST /auth/login` and `POST /auth/tenant/{schoolCode}` (tenant lookup by school code) are public; everything else requires `Authorization: Bearer <token>`. `JwtAuthenticationFilter` parses the token into a `Teacher` principal and puts a `ROLE_<roleId>` authority into the `SecurityContext`; controllers pull `teacherId`/`tenantId`/`roleId` back out via `JwtHelper.extractTeacherId`/`extractTenantId`. `POST /auth/renew-session` exchanges a refresh token for a new access token.
- **Student (mobile app)** — endpoints under `/api/...`. `POST /api/auth/login` and `POST /api/auth/forgot-password` are public; everything else under `/api` requires a student JWT. Controllers extract `studentId`/`tenantId` directly from the token via `JwtHelper.extractStudentId` (student tokens don't populate a `SecurityContext` principal the way teacher tokens do — they're validated by the same stateless filter but read manually in each controller).

> **Known issue:** `SecurityConfiguration` permits the literal string `"lookup/role"` (no leading slash) as a public path — this doesn't match any real request path and doesn't correspond to an actual endpoint (`LookupController` only exposes `GET /lookup/roles`, plural, which still requires authentication). Don't rely on `/lookup/role` being public; it isn't a real, working endpoint.

## API Overview

All responses are wrapped in a common `ResponseObject<T>` (`{ success, errors, data }`).

### Auth (`/auth` — teacher, `/api/auth` — student)
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/auth/tenant/{schoolCode}` | Public | Look up a tenant by school code |
| POST | `/auth/login` | Public | Teacher login → JWT + refresh token |
| POST | `/auth/forgot-password` | Public | Teacher forgot-password email |
| POST | `/auth/reset-password` | Teacher | Reset the logged-in teacher's password |
| POST | `/auth/renew-session` | Teacher | Exchange a refresh token for a new access token |
| POST | `/api/auth/login` | Public | Student login → JWT + refresh token |
| POST | `/api/auth/forgot-password` | Public | Student forgot-password email |
| POST | `/api/auth/change-password` | Student | Change the logged-in student's password |
| GET | `/api/auth/school/contact` | Student | Current tenant's contact info |

### Students (`/student`, teacher-side)
| Method | Path | Description |
|---|---|---|
| GET | `/student/list` | Paged student list (`page`, `size`, `search`, `classId`, `sectionId`, `sortBy`, `sortDir`) |
| GET | `/student/{studentId}` | Get a student by ID |
| POST | `/student` | Create/update a student |
| DELETE | `/student/{studentId}` | Delete a student |

### Teachers (`/teacher`)
| Method | Path | Description |
|---|---|---|
| GET | `/teacher/list` | List teachers for the current tenant |
| GET | `/teacher/subject/{subjectId}` | Teachers assigned to a subject |
| GET | `/teacher/{teacherId}` | Get a teacher by ID |
| POST | `/teacher` | Create/update a teacher |
| DELETE | `/teacher/{teacherId}` | Delete a teacher |

### Classes (`/class`)
| Method | Path | Description |
|---|---|---|
| GET | `/class/list` | List classes for the current tenant |
| GET | `/class/{classId}` | Get a class by ID |
| GET | `/class/{classId}/subjects` | Subjects assigned to a class |
| POST | `/class` | Create/update a class |
| DELETE | `/class/{classId}` | Delete a class |

### Timetable (`/timetable`)
| Method | Path | Description |
|---|---|---|
| GET / POST | `/timetable/working-days` | Get/save the tenant's working days |
| POST | `/timetable/time-slots` | Save time slots for a class |
| GET | `/timetable/time-slots/{classId}` | List time slots for a class |
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

### Dashboard (`/dashboard`)
| Method | Path | Description |
|---|---|---|
| GET | `/dashboard/summary` | Tenant-wide dashboard summary |

### Attendance (`/attendance`)
| Method | Path | Description |
|---|---|---|
| GET | `/attendance/roster/{classId}` | Attendance roster for a class/section/date (`sectionId`, `date`) |
| POST | `/attendance` | Save attendance for a class (marked by the logged-in teacher) |
| GET | `/attendance/summary/{classId}` | Attendance summary (`sectionId`, `fromDate`, `toDate`) |

### Exams (`/exam`)
| Method | Path | Description |
|---|---|---|
| GET | `/exam/list` | List exams (optional `classId`) |
| GET | `/exam/{examId}` | Get an exam by ID |
| POST | `/exam` | Save/update an exam (and its subject schedule) |
| DELETE | `/exam/{examId}` | Delete an exam |
| GET | `/exam/{examId}/marks/{classId}` | Marks-entry sheet (`sectionId`) |
| POST | `/exam/marks` | Save marks for a class |
| GET | `/exam/{examId}/report/{studentId}` | Report card for a student |

### Fees (`/fee`, teacher-side)
| Method | Path | Description |
|---|---|---|
| GET | `/fee/status/{classId}` | Fee status for a class (`sectionId`) |
| POST | `/fee/payment` | Record a fee payment (collected by the logged-in teacher) |
| GET | `/fee/history/{studentId}` | Payment history for a student |

### Homework (`/homework`)
| Method | Path | Description |
|---|---|---|
| GET | `/homework/list/{classId}` | Homework list (`sectionId`) |
| POST | `/homework` | Save homework — `multipart/form-data`, see below |
| DELETE | `/homework/{homeworkId}` | Delete homework |

### Notes (`/note`)
| Method | Path | Description |
|---|---|---|
| GET | `/note/list/{classId}` | Note list (`sectionId`) |
| POST | `/note` | Save a note — `multipart/form-data`, see below |
| DELETE | `/note/{noteId}` | Delete a note |

`HomeworkController` (`POST /homework`) and `NoteController` (`POST /note`) accept `multipart/form-data`: a `data` part with the JSON request body and an optional `file` part for the attachment. When a file is present, `HomeworkServiceImpl`/`NoteServiceImpl` upload it via `FileStorageService` (Cloudinary or AWS S3, per `is-live`) and store the resulting URL as `attachmentUrl`.

### Announcements (`/announcement`)
| Method | Path | Description |
|---|---|---|
| GET | `/announcement/list` | List announcements for the tenant |
| POST | `/announcement` | Save an announcement — immediate or scheduled (see Scheduled Jobs) |
| DELETE | `/announcement/{announcementId}` | Delete an announcement |

### Leave — teacher review (`/leave`)
| Method | Path | Description |
|---|---|---|
| GET | `/leave/list/{classId}` | Leave requests for a class (`sectionId`) |
| PATCH | `/leave/{leaveId}/status` | Approve/reject a leave request |

### Leave — student self-service (`/api/student/leave`)
| Method | Path | Description |
|---|---|---|
| GET | `/api/student/leave/list` | The logged-in student's own leave requests |
| POST | `/api/student/leave` | Submit a new leave request |
| DELETE | `/api/student/leave/{leaveId}` | Delete an own, still-pending leave request |

### Mobile / student self-service (`/api/student`)
| Method | Path | Description |
|---|---|---|
| GET | `/api/student/home` | Home dashboard |
| GET | `/api/student/timetable` | Timetable (optional `day`) |
| GET | `/api/student/exams` | Upcoming/past exams |
| GET | `/api/student/homework` | Homework list (`fromDate`, `toDate`) |
| GET | `/api/student/notes` | Notes list (`fromDate`, `toDate`) |
| GET | `/api/student/attendance` | Attendance history (`fromDate`, `toDate`) |
| GET | `/api/student/homework/{homeworkId}` | Homework detail |
| GET | `/api/student/notes/{noteId}` | Note detail |
| GET | `/api/student/results` | Exam results summary |
| GET | `/api/student/results/{examId}` | Report card detail |
| GET | `/api/student/announcements` | Announcements visible to the student |
| GET | `/api/student/notifications` | Paged in-app notifications (`page`, `size`) |
| PATCH | `/api/student/notifications/{notificationId}/read` | Mark a notification read |
| GET | `/api/student/notifications/unread-count` | Unread notification count |
| GET | `/api/student/{studentId}` | Student detail by ID |

### Mobile fees (`/api/student/fee`)

Handles the mobile fee-payment flow: `GET /detail` (pending/paid summary), `POST /create-order` (creates a Razorpay order for the pending or a partial amount), `POST /verify-payment` (verifies the Razorpay signature and records the payment — this endpoint itself carries no JWT-derived identity, only the order ID/payment ID/signature). Business logic for order creation/verification lives in `FeeService`, which delegates the Razorpay-specific parts to `RazorpayConfiguration`.

### Push notification device registration (`/api/student/fcm-token`)

`FcmTokenController` lets the mobile app register/unregister the current device for push notifications: `POST /api/student/fcm-token` (upsert the device's FCM token, keyed by token so re-registering the same device just re-points it to whichever student is logged in) and `DELETE /api/student/fcm-token?fcmToken=...` (remove a token, e.g. on logout). Both derive `studentId`/`tenantId` from the JWT.

Push notifications aren't a separate controller — they're triggered by other actions (announcement publish, exam scheduling/results, homework/note creation, leave status changes, birthdays). Each of those calls `StudentNotificationService.notify(...)`, which persists a `StudentNotification` row and then calls `FirebaseConfig.sendToStudents(...)` directly (this used to be a separate `FcmPushService` — that logic now lives on `FirebaseConfig` itself, alongside its client initialization, matching the same "client bean + business methods, no separate service interface" pattern used for Razorpay/Cloudinary/AWS). `sendToStudents` loads the tenant's registered device tokens (`StudentDeviceTokenRepository`), batches them (500 tokens per Firebase multicast call), and sends via `FirebaseMessaging`. If `firebase.credentials-file` isn't configured, `FirebaseConfig.isEnabled()` is `false` and sends are skipped (logged, not thrown). Tokens Firebase reports as unregistered/invalid are deleted automatically after a send.

## Scheduled Jobs (`scheduler/`)

| Class | Schedule | Behavior |
|---|---|---|
| `AnnouncementScheduler` | Every 60 seconds | Finds announcements with status `SCHEDULED` whose `publishDate` has arrived, flips them to `PUBLISHED`, and triggers their push notification. |
| `BirthdayNotificationScheduler` | Daily at 6:00 AM (`Asia/Kolkata`) | Finds students with a birthday today and sends each one a "Happy Birthday" in-app notification + push. |

## Domain Model (key entities)

`Tenant`, `Role`, `Teacher`, `Student`, `ClassMaster`, `ClassSection`, `ClassSubject`, `ClassFee`, `Subject`, `TeacherClass`, `TeacherSubject`, `StudentClass`, `AcademicYear`, `EmploymentType`, `WorkingDay`, `TimeSlot`, `Timetable`, `Announcement`, `Attendance`, `Exam`, `ExamMark`, `ExamSchedule`, `Homework`, `Note`, `Leave` (student leave request — pending/approved), `FeePayment`, `RazorpayOrder`, `RazorpayTransaction`, `PaymentWebhookLog`, `StudentDeviceToken` (one row per registered mobile device, keyed by FCM token, used for push notification delivery), `StudentNotification` (in-app notification record — title, body, type, read state).

## Error Handling

`CustomException` (carries a `param` + `msg`) is caught by `CustomExceptionHandler` (`@ControllerAdvice`) and turned into an **HTTP 400** response: `ResponseObject{ success: false, data: null, errors: [{ param, msg }] }`. There is no generic/catch-all exception handler — anything other than `CustomException` falls through to Spring Boot's default error handling instead of the standard envelope, so services should raise `CustomException` for all expected business/validation errors.

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
  The one exception is a short-lived stream/lambda parameter used only inline where the surrounding context makes the type obvious.
- **Service implementations should stay simple and beginner-friendly: prefer plain `for`/`if` loops over streams.** Avoid `.stream()`/`.collect()` chains, `Map.computeIfAbsent`/`merge`, `Consumer`/`Function`-style helper parameters, and arrow-`switch` expressions in `service/*Impl.java` classes — write out the loop or `if/else` instead, even if it's a few more lines. Idiomatic one-liners like `.orElseThrow(() -> ...)`, `.orElse(...)`, and `Comparator.comparing(...)` used only for `.sort()` are fine to keep, since rewriting those as manual loops adds complexity rather than removing it.
- **DTO naming depends on whether the shape is one-way or shared.** If a DTO is only ever sent to the server, name it `XxxRequest`; if it's only ever sent back, name it `XxxResponse`. If the same class is used as *both* the save-request body and the get-by-id response (a common shortcut when create/read share a shape), name it `XxxDTO` instead — `XxxRequest` returned from a GET endpoint reads backwards. Example: `Student` save/get both use `StudentDTO`, not `StudentRequest`.
- **Fixed-vocabulary status/type codes belong in `constant/Constant.java`**, not as string literals scattered across services (e.g. attendance `P`/`A`/`L`/`H`, leave `PENDING`/`APPROVED`, announcement `PUBLISHED`/`SCHEDULED`, payment mode/status, notification `type` discriminators). Add a new constant there rather than hardcoding a new status string inline.
- **`BeanUtils.copyProperties(source, target)` is the established pattern for bulk field copies** between an entity and a same-shaped DTO (see `TeacherServiceImpl`, `ExamServiceImpl`, `StudentServiceImpl`, `FeeServiceImpl`, `AuthServiceImpl`). Before using it, check whether the source has any field the target should *not* receive (e.g. `password`, or a primary key on an update path) — if so, pass it as an `ignoreProperties` vararg (`BeanUtils.copyProperties(request, entity, "teacherId", "password")`) rather than skip the helper and hand-copy every field.

### Layout & conventions

- Layers: `controller/` → `service/` (interface + `Impl`) → `repository/` → `entity/`; DTOs in `dto/<module>/`.
- Every response is wrapped in `ResponseObject<T>` (`{success, errors, data}`).
- Errors: throw `new CustomException("<param>", "<message>")` → HTTP 400 with `errors[0].msg`.
- Multi-tenant: extract `tenantId` from the JWT and scope every query by it.
- **Mobile (student) API lives under `/api/...`** (`MobileAuthController`, `MobileStudentController`, `MobileSchoolController`, `MobileFeeController`, `FcmTokenController`, `LeaveController`); web endpoints are unprefixed and authenticate Teachers.
- `ddl-auto=update`, no migrations — new entity columns must be nullable.
- Shared, genuinely-duplicated helper logic lives in `helper/CommonHelper` — don't reintroduce per-service copies of logic that already exists there. Current methods: `getCurrentYear`, `teacherNameForId`/`teacherNameForTeacher`, `studentNameForId`/`studentNameForStudent`, `displayClassForIds`/`displayClassForStudentClass`, `fullAddressForTenant`/`fullAddressForStudent`, `rollNo`, `generateRandomPassword`, `generateAdmissionNo`, `generateUsername`, `subjectName`.
