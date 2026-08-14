# EduNest — Combined Project Documentation

This file merges the READMEs of all three EduNest repositories (API, mobile app, admin web
panel) plus a changelog of the refactoring/documentation work done across them in this
session. It is a snapshot for reference — the authoritative, up-to-date docs remain each
repo's own `README.md`.

| Project | Role |
|---|---|
| `EduNest-Api` | Spring Boot REST API — serves both other apps |
| `EduNest-App` | Flutter mobile app — students |
| `EduNest-Web` | React admin panel — teachers / staff |

---

# Part 1 — Session Changelog (what was already done)

Work completed across all three repos in this session, in order:

## `EduNest-Api` (Spring Boot backend)

1. **Push notification refactor** — deleted `FcmPushService`/`FcmPushServiceImpl`; moved
   the send logic (`sendToStudents`, batching, invalid-token cleanup) directly into
   `FirebaseConfig`, matching the existing pattern used by `AwsConfiguration` (client bean +
   business methods, no separate service interface). Updated `StudentNotificationServiceImpl`
   to call `firebaseConfig.sendToStudents(...)` directly.
2. **`CommonHelper` consolidation** — found and eliminated duplicated logic scattered across
   services:
   - `firstName + " " + lastName` name-building — added `teacherNameForTeacher(Teacher)` /
     `studentNameForStudent(Student)` (entity-based, no extra DB hit), replacing manual
     concatenation in `FeeServiceImpl`, `LeaveServiceImpl`, `StudentServiceImpl`,
     `TeacherServiceImpl`, `TimetableServiceImpl`, `JwtHelper`.
   - Class/section "display class" building — **5 duplicate copies** (`FeeServiceImpl`,
     `LeaveServiceImpl`, `MobileStudentServiceImpl` ×3 including two inlined-not-even-extracted
     copies) collapsed into `CommonHelper.displayClassForIds(classId, sectionId)` /
     `displayClassForStudentClass(StudentClass)`.
   - `buildStudentName` duplicate in `MobileStudentServiceImpl` → replaced with
     `CommonHelper.studentNameForStudent`.
   - Tenant/Student address-building duplicates (`FeeServiceImpl.buildSchoolAddress`,
     `MobileStudentServiceImpl.buildFullAddress`) → `CommonHelper.fullAddressForTenant` /
     `fullAddressForStudent`, sharing a private `fullAddressForCommon(...)` core.
3. **Removed Java method overloading in `CommonHelper`** (per explicit request) — every
   previously-overloaded pair got a distinct `ForX`-suffixed name instead
   (`teacherNameForId`/`teacherNameForTeacher`, `studentNameForId`/`studentNameForStudent`,
   `displayClassForIds`/`displayClassForStudentClass`, `fullAddressForTenant`/
   `fullAddressForStudent`/`fullAddressForCommon`). Updated every call site project-wide.
4. **Removed unused code** — one unused import (`java.util.Set` in `LeaveServiceImpl`); a
   full sweep found no other unused private methods/fields/imports/dead classes/commented-out
   code blocks.
5. **Centralized hardcoded status/type strings into `constant/Constant.java`** — added and
   wired up constants for: leave status (`PENDING`/`APPROVED`), announcement status
   (`PUBLISHED`/`SCHEDULED`), attendance *display* status (distinct from the existing DB codes
   `P`/`A`/`L`/`H`), exam schedule status (`Completed`/`Upcoming`), exam result
   (`PASS`/`FAIL`), fee payment mode (`CASH`/`ONLINE`), payment status (`PAID`/`FAILED`), JWT
   user type (`STUDENT`), and the push-notification `type` discriminators (`NOTIFICATION` plus
   per-category codes: `EXAM_SCHEDULED`, `RESULT_PUBLISHED`, `LEAVE_STATUS`, `NOTE`,
   `HOMEWORK`, `ANNOUNCEMENT`, `BIRTHDAY`). Updated every call site across ~12 files,
   including entity default values (`Leave.status`, `Announcement.status`).
6. **`BeanUtils.copyProperties` cleanup** — replaced large manual field-copy blocks with
   `BeanUtils.copyProperties(source, target)` in `StudentServiceImpl`, `TeacherServiceImpl`,
   and `ExamServiceImpl`. Each was checked field-by-field for unsafe same-named-field
   collisions before converting:
   - `StudentServiceImpl.getStudentById` — excluded `"password"` (`StudentDTO` doubles as the
     GET response; an unqualified copy would have leaked the password hash to the client).
   - `TeacherServiceImpl.saveTeacher` — excluded `"teacherId"` (would have nulled the primary
     key on update, turning it into an unintended insert) and `"password"` (would have
     overwritten the hashed password with plaintext on every edit).
   - `ExamServiceImpl` — confirmed safe (type mismatches make `BeanUtils` skip the fields that
     needed special handling anyway).
   - **Declined** to convert a mixed-source block in `MobileStudentServiceImpl.getResults()`
     (fields sourced from two different objects with several mismatched names) — would have
     silently dropped fields. Extracted it into a `buildExamResultItem(exam, reportCard)`
     private method instead, per request.
7. **`README.md` rewritten** — added full documentation for the previously-undocumented
   `Leave` module (`LeaveController` + `LeaveTeacherController`), the `scheduler/` package
   (`AnnouncementScheduler`, `BirthdayNotificationScheduler`), missing entities (`Leave`,
   `StudentNotification`), corrected the stale `FirebaseConfig` description, flagged a real
   `SecurityConfiguration` bug (a dead `"lookup/role"` permit-rule that doesn't match any
   actual endpoint), added the `security.jwt.student-expiration-time` property, a full
   dependency table, and an explicit "no CI/Docker/Swagger/Postman" statement.

## `EduNest-App` (Flutter mobile)

Patched (not rewritten) the existing README to close real gaps found against the actual
`lib/` source:
- Added missing pubspec dependencies to the table: `url_launcher`, `table_calendar`,
  `firebase_core`, `firebase_messaging`, `flutter_local_notifications`.
- Documented the entirely-missing **Leave** feature (`leave_list_page`, `leave_request_page`,
  `LeaveRepo`), **Announcements** feature, and the full **push notification** pipeline
  (`NotificationService` — FCM init, foreground/background/tap handlers, local banner display,
  permission request, token sync on login/refresh, unregister on logout).
- Expanded the architecture tree and the student-facing API table with the endpoints that
  back these features (attendance, results, announcements, notifications ×3, leave ×3,
  fcm-token ×2).

## `EduNest-Web` (React admin panel)

Patched the existing README:
- **Fixed a stale claim** — the "Modules" list included "Events", but no such module exists
  anywhere in `src/pages` or `src/sections`; removed it. A second, related stale reference
  in "Notes / gotchas" (claiming the calendar template leftover was replaced by an "Events"
  module) was also corrected — there is no live calendar-style feature in this app at all.
- **Added the missing "Leave Requests" module** — teacher-side review/approve/reject of
  student-submitted leave requests (`getLeaveListAsync`/`updateLeaveStatusAsync`), confirmed
  present in both the nav config and `ApiService.js`.
- **Documented unused dependencies** — `firebase` and `@auth0/auth0-react` are installed but
  dead code; `global-config.js` supports switching `auth.method` to `firebase`/`auth0`/
  `amplify`, but this app is configured for `jwt` (the custom backend flow), so neither ever
  activates.
- **Fixed a naming inconsistency** — the table and footer referred to `EduNest-backend`;
  corrected to the actual repo name, `EduNest-Api`.

---

# Part 2 — `EduNest-Api` README

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

---

# Part 3 — `EduNest-App` README (Flutter mobile)

# EduNest — Student Mobile App

Flutter mobile app for **students** of the EduNest school ERP. Students connect to
their school with a school code, sign in with a username and password, and can view
their profile, timetable, homework, notes, exam schedule, results, fee status, and
school contact info — and pay pending fees via Razorpay.

Part of a three-app system:

| Project | Role |
|---|---|
| **EduNest-App** (this repo) | Flutter mobile app — **students** |
| `EduNest-Web` | React admin panel — teachers / staff |
| `EduNest-Api` | Spring Boot REST API (serves both) |

## Getting started

```bash
flutter pub get
flutter run
flutter analyze
```

Requires the Flutter SDK (Dart `^3.11.4`).

### Environment / API base URL

The environment is selected in `lib/main.dart` via `EduNestEnvironment.initialize(env: 'dev')`.
Base URLs live in `lib/flavors/edunest_environment.dart`:

| env | base URL |
|---|---|
| `dev` | `http://10.185.117.76:8081` (local backend, port 8081) |
| `uat` | `https://uat-api.mynovian.com` |
| `prod`| `https://api.mynovian.com` |

> The `dev` URL is cleartext `http://`. Android release and iOS block cleartext by
> default, so it may work in debug but fail in release without the network config in
> `android/.../network_security_config.xml` and `ios/Runner/Info.plist`.

## Dependencies

| Package | Used for |
|---|---|
| `dio` | HTTP client |
| `get` | Navigation + snackbars (`GetMaterialApp`) |
| `shared_preferences` | Local storage (token, tenant, student) |
| `cached_network_image` | Disk-cached school banner / logo / photo |
| `device_info_plus` | Real device details on the Device Info screen; also used to derive the `deviceId` sent when registering an FCM token |
| `permission_handler` | Location/notification permission requests on first Home open |
| `package_info_plus` | App version |
| `google_fonts`, `intl` | Typography, date formatting |
| `razorpay_flutter` | In-app checkout UI for the Fee Payment screen |
| `url_launcher` | Opening external links (e.g. school contact phone/email) |
| `table_calendar` | Calendar widget (attendance / date-range pickers) |
| `firebase_core`, `firebase_messaging` | Firebase init + push notification delivery (`NotificationService`) |
| `flutter_local_notifications` | Shows a local banner when a push arrives while the app is in the foreground |

## Architecture

Screens are `StatefulWidget` + `setState`, holding a repository directly — no GetX
controllers, no result-wrapper types.

```
lib/
  main.dart                       app entry — Firebase init, NotificationService.initialize(),
                                    selects env, runs MyApp
  firebase_options.dart           generated Firebase platform config (flutterfire configure)
  app/
    my_app.dart                   GetMaterialApp, theme
    core/
      base/base_repo.dart         BaseRepo (marker base class)
      network/
        dio_client.dart           DioClient.getInstance() -> fresh Dio + interceptor
        edunest_interceptors.dart attaches Bearer token; 401 -> clear + TenantPage
        error_helper.dart         ApiException + ErrorHelper.toApiException(e)
      services/
        common_service.dart       SharedPreferences: token, tenant, student, schoolCode
        subject_icon_service.dart maps a subject name -> icon + color pair
        notification_service.dart FCM init, foreground/background/tap handlers, local banner
                                    display, permission request, token sync on login/refresh
      helper/
        date_util.dart            date formatting helpers
        homework_status_helper.dart due-date -> "Due Today"/"Due Tomorrow"/"Due Completed" badge
      utils/
        app_urls.dart              AppUrls.someCall() -> full URL strings, grouped by module
        app_constants.dart         misc shared constants
      values/                      app_colors.dart, app_values.dart
    data/
      model/                       auth/, student/, homework/, exam/, fee/, timetable/, profile/,
                                    leave/, notification/, announcement/, attendance/
      repository/                  auth_repo, tenant_repo, profile_repo, features_repo, fee_repo,
                                    leave_repo, fcm_repo
    global_widgets/
      edunest_button / _text_field / _divider / _confirm_dialog / _date_picker / _empty_state
      edunest_filter.dart          shared bottom-sheet filter (This Week/Month/Custom Range),
                                    used by Homework, Notes, and Attendance
    UI/
      splash/                      SplashScreen (routes by stored token/tenant)
      login/                       tenant_page, login_page, forgot_password widget
      home/                        home_page (feature grid + attendance stats) + drawer_menu
      features/
        homework/                  homework_page (Date Wise / Subject Wise tabs + filter),
                                    homework_detail_page
        notes/                     notes_page (+ filter), notes_detail_page
        fee/                       fee_payment_page, fee_amount_dialog, fee_payment_handler
        leave/                     leave_list_page (own leave requests), leave_request_page (submit)
        announcement/              announcements_page, announcement_detail_page
        attendance_page.dart, exam_schedule_page.dart, results_page.dart (+ result_detail_page),
        timetable_page.dart
      profile/                     profile, school_contacts, faq, about_us, settings*
      notifications/               notification_page (paged list, mark-as-read)
  flavors/                         environment + global configuration
```

### Networking pattern

```dart
// repository — thin, owns the try/catch, throws ApiException
class AuthRepo extends BaseRepo {
  Future<LoginResponseModel> login(String username, String password) async {
    try {
      final res = await DioClient.getInstance().post(
        AppUrls.login(),
        data: {"username": username, "password": password},
      );
      return LoginResponseModel.fromJson(res.data['data']);
    } catch (e) {
      throw ErrorHelper.toApiException(e);
    }
  }
}
```

```dart
// screen — StatefulWidget holds the repo, catches ApiException for inline errors
final AuthRepo _authRepo = AuthRepo();
try {
  final result = await _authRepo.login(username, password);
  // save + navigate
} on ApiException catch (e) {
  setState(() => passwordError = e.message);
}
```

The API envelope is `{ success, errors, data }`; repositories read `res.data['data']`.
`EdunestInterceptors` adds `Authorization: Bearer <token>` from `CommonService`, and on
`401` clears storage and returns the user to the Tenant page.

## App flow

```
Splash ──► token stored?  ──yes──►  Home
        └► tenant stored? ──yes──►  Login   (enter username + password)
        └► otherwise      ─────────►  Tenant  (enter school code)
```

1. **Tenant** — enter school code → `GET /auth/tenant/{schoolCode}` → saves the school
   (name, logos, banner) to local storage.
2. **Login** — username + password → `POST /api/auth/login` → saves session/refresh
   tokens, student profile, and tenant. Shows the school's logo and name.
   *Forgot password* emails a new password to the registered address. On success,
   `NotificationService.syncFcmToken()` uploads the device's current FCM token so pushes
   can be delivered to this session.
3. **Home** — feature grid (Time Table, Exam, Marks & Results, Announcements, Home
   Work, Notes, Fee Details, Leave) plus today's/monthly/average attendance stats. On
   first open, prompts for location and notification permissions (native OS dialogs,
   asked once — `NotificationService.requestNotificationPermission()`).
4. **Homework / Notes** — list with **Date Wise** and **Subject Wise** tabs (Homework
   only; Notes is a flat list), a filter (This Week / This Month / Custom Date Range —
   Homework defaults to "last 2 days" on first open) that re-queries the API with
   `fromDate`/`toDate`, and a detail screen per item.
5. **Attendance** — dedicated screen (reachable from Home) showing the same
   today's/monthly/average stats plus a per-day history.
6. **Announcements** — list + detail screen for announcements published by the school.
7. **Leave** — `leave_list_page` shows the student's own submitted leave requests
   (pending/approved) with delete for still-pending ones; `leave_request_page` submits a
   new request (date + reason).
8. **Notifications** — `notification_page` shows a paged list of in-app notifications
   (announcements, exam scheduling/results, homework/notes, leave status changes,
   birthdays), each markable as read. Pushes arrive via `NotificationService`
   (Firebase Messaging): shown as a local banner in foreground, handled by the OS in
   background, and tapping one — foreground, background, or from a terminated state —
   opens this screen.
9. **Fee Details** — shows pending/paid amount, then "Pay" opens the Razorpay checkout
   UI; on success the app verifies the payment with the backend and shows the result.
10. **Profile / Settings** — student profile, school contacts, change password, device
    info, FAQ, about us. Logging out calls `NotificationService.unregisterFcmToken()` to
    remove this device's token from the backend first.

## Student-facing API (all under `/api`, except the pre-login school lookup)

| Endpoint | Purpose |
|---|---|
| `GET /auth/tenant/{schoolCode}` | Resolve school by code (public, pre-login) |
| `POST /api/auth/login` | Login with `{username, password}` |
| `POST /api/auth/forgot-password` | Email a new password |
| `POST /api/auth/change-password` | Change password (authenticated) |
| `GET /api/auth/school/contact` | School contact details |
| `GET /api/student/home` | Home screen summary + attendance stats |
| `GET /api/student/{studentId}` | Full student profile |
| `GET /api/student/timetable` | Timetable (optional `day` query param) |
| `GET /api/student/exams` | Upcoming/past exams |
| `GET /api/student/homework` | Homework list (optional `fromDate`/`toDate`) |
| `GET /api/student/homework/{homeworkId}` | Homework detail |
| `GET /api/student/notes` | Notes list (optional `fromDate`/`toDate`) |
| `GET /api/student/notes/{noteId}` | Note detail |
| `GET /api/student/attendance` | Attendance history |
| `GET /api/student/results` | Exam results summary |
| `GET /api/student/results/{examId}` | Report card detail |
| `GET /api/student/announcements` | Announcements list |
| `GET /api/student/notifications` | Paged in-app notifications |
| `PATCH /api/student/notifications/{notificationId}/read` | Mark a notification read |
| `GET /api/student/notifications/unread-count` | Unread notification count |
| `GET /api/student/leave/list` | The logged-in student's own leave requests |
| `POST /api/student/leave` | Submit a new leave request (`leaveDate`, `reason`) |
| `DELETE /api/student/leave/{leaveId}` | Delete a still-pending leave request |
| `POST /api/student/fcm-token` | Register this device's FCM token (`fcmToken`, `deviceId`, `platform`) |
| `DELETE /api/student/fcm-token` | Unregister this device's FCM token (before logout) |
| `GET /api/student/fee/detail` | Fee summary (total/paid/pending) |
| `POST /api/student/fee/create-order` | Create a Razorpay order for a fee payment |
| `POST /api/student/fee/verify-payment` | Verify a completed Razorpay payment |

## Assets

Bundled images live in `assets/images/` and are declared in `pubspec.yaml`
(`full-icon.png`, `BackGroud.png`, `ChangePassword.png`).

---

# Part 4 — `EduNest-Web` README (React admin panel)

# EduNest — Admin Web Panel

React + MUI admin panel for the EduNest school ERP. Used by **teachers and staff** to
manage classes, students, teachers, attendance, fees, exams, homework, announcements,
leave requests, and a dashboard overview.

Part of a three-app system:

| Project | Role |
|---|---|
| **EduNest-Web** (this repo) | React admin panel — teachers / staff |
| `EduNest-App` | Flutter mobile app — students |
| `EduNest-Api` | Spring Boot REST API (serves both) |

## Getting started

```bash
npm install        # or: yarn
npm start          # dev server — HTTPS on https://localhost:3030
npm run build      # production build (vite)
npm run lint:fix   # eslint --fix
npm run fm:fix     # prettier
npm run fix:all    # lint:fix + fm:fix
```

- Node **20.x**, package manager **yarn 1.22**.
- **The dev server runs HTTPS** using local certs at `config/certs/localhost.{key,crt}`
  (see `vite.config.js`). If the server won't boot, those cert files are the usual cause.
- Set the API base URL via the `VITE_SERVER_URL` env var (in a `.env` file).

## Stack

- **React 18** + **Vite 6** (`@vitejs/plugin-react-swc`), plain **JavaScript/JSX** (no TypeScript)
- **MUI v6** (+ `@mui/lab`, `x-data-grid`, `x-date-pickers`) — built on the *Minimals* admin template
- **Redux Toolkit** + react-redux for global state
- **react-hook-form** + **zod** for forms, **axios** for HTTP
- **ApexCharts** for charts, `sonner` for toasts, `i18next` for i18n

Import alias: `src/...` maps to the `src` directory (`vite.config.js` + `jsconfig.json`).

## Architecture

### API layer

Two files own all backend communication:

- **`src/services/ApiService.js`** — every endpoint is a small named async function
  (`getStudentListAsync`, `saveClassAsync`, …) exported from one default object.
  Add new endpoints here, grouped by the existing `//Student`, `//Fee` style comments.
- **`src/services/AxiosService.js`** — axios defaults + interceptors:
  - attaches `Authorization: Bearer <sessionToken>` from the Redux store;
  - **unwraps the envelope** — components receive `{ data }` where `data` is the
    backend's inner `data` field, not the full `{success, errors, data}` body;
  - on 401 attempts a refresh via `renewSessionAsync`, then logs out on failure;
  - validation / bad-request errors resolve (not reject) as `{ data, errors }`, so
    callers check `errors` rather than using try/catch.

Base URL comes from `VITE_SERVER_URL`.

### Routing

- **`src/routes/paths.js`** is the single source of truth for URLs — never hardcode a
  path; add it here and reference `paths.dashboard.<module>`.
- Route trees live in `src/routes/sections/` (`auth`, `dashboard`, `main`, `guest`).
- Nav / menu items: `src/layouts/nav-config-dashboard.jsx`.

### Feature code layout

Each module is split across two directories:

- **`src/pages/<module>/`** — thin route components (`list.jsx`, `new.jsx`, `edit.jsx`)
- **`src/sections/<module>/`** — the real UI: `view/` (page views), plus dialogs,
  forms and table rows (e.g. `student-save-form.jsx`, `event-form-dialog.jsx`)

```
src/
  services/         ApiService.js, AxiosService.js
  routes/           paths.js, sections/ (auth, dashboard, main, guest)
  pages/<module>/   route components
  sections/<module>/ views, dialogs, table rows
  store/            Redux: authReducer, appReducer, snackbar
  auth/             JWT views, guards (auth / guest / role-based), context
  components/       template component library (hook-form, table, iconify, upload, …)
  layouts/          dashboard layout + nav-config
  utils/            enums.js, constants.js, utils.js, format helpers, azureBlob.js
  theme/  locales/  global-config.js
```

## Modules

Dashboard, Classes, Teachers, Students, Timetable, Attendance, Fees, Exams,
Announcements, Homework, Notes, Leave Requests. (Nav order in `nav-config-dashboard.jsx`.)

**Leave Requests** (`src/pages/leave`, `src/sections/leave`) — teacher-side review of
student-submitted leave requests: list by class/section (`getLeaveListAsync`) and
approve/reject (`updateLeaveStatusAsync`). Students submit these from the mobile app;
there's no leave-creation UI here.

The **Dashboard** (`src/sections/analytics`) reads `GET /dashboard/summary` for
student/teacher/class counts, today's attendance, monthly fee collection, upcoming
events, and latest announcements.

## Conventions

- Endpoint functions end in `Async` and live only in `ApiService.js`.
- Paths only from `paths.js`.
- Forms use `react-hook-form` with the `src/components/hook-form` wrappers and `zod` schemas.
- Components are `.jsx`; PropTypes are intentionally not used.

## Notes / gotchas

- This codebase was **adapted from a dental product** ("Dentory"). Some leftovers still
  exist (`src/pages/auth/dentist-*.jsx`, dental role names in `src/utils/enums.js`) —
  don't treat these as EduNest features.
- `src/sections/calendar/` is unused Minimals template code (imports uninstalled
  `@fullcalendar/*`) — there is no "Events" module and no live calendar-style feature
  in this app; don't reference either.
- `enums.roleType` and `enums.displayRole` disagree with each other — verify against the
  backend `role` table before relying on either.
- `firebase` and `@auth0/auth0-react` are dependencies but **unused** — `src/global-config.js`
  supports `auth.method: jwt | amplify | firebase | auth0` as a template feature, and this
  app is configured for `jwt` (the custom backend JWT flow described above). `src/lib/firebase.js`
  only initializes if `auth.method === 'firebase'`, which it never is here.

## Related projects

- `EduNest-Api` — Spring Boot API this app calls
- `EduNest-App` — Flutter mobile app (students)
