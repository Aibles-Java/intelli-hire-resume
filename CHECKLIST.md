# IntelliHire Resume Service — Implementation Checklist

> Tham chiếu: `ROADMAP.md`
> Cập nhật theo tiến độ thực tế. Tick `[x]` khi hoàn thành.

---

## Tổng Tiến Độ

| Step | Nội Dung | Trạng Thái |
|------|----------|-----------|
| STEP 0 | Pre-work (fix base code) | ✅ Hoàn thành |
| STEP 1 | ParseJob Service + Controller | ✅ Hoàn thành |
| STEP 2 | File Management (MinIO + Redis) | ⬜ Chưa bắt đầu |
| STEP 3 | Contact Management | ⬜ Chưa bắt đầu |
| STEP 4 | Experience Management | ⬜ Chưa bắt đầu |
| STEP 5 | Education Management | ⬜ Chưa bắt đầu |
| STEP 6 | Skills (Catalog + CRUD + Profile) | ⬜ Chưa bắt đầu |
| STEP 7 | Content Extraction Engine | ⬜ Chưa bắt đầu |
| STEP 8  | Production Readiness                       | ⬜ Chưa bắt đầu |
| STEP 9  | Angular Project Setup + Core Architecture  | ⬜ Chưa bắt đầu |
| STEP 10 | Resume & ParseJob UI                       | ⬜ Chưa bắt đầu |
| STEP 11 | File Upload + Contact/Exp/Edu UI           | ⬜ Chưa bắt đầu |
| STEP 12 | Skills & Skill Profile UI                  | ⬜ Chưa bắt đầu |
| STEP 13 | Frontend Production Readiness              | ⬜ Chưa bắt đầu |

---

## STEP 0 — Pre-work (Fix Base Code)

> Bắt buộc hoàn thành trước khi implement bất kỳ feature nào.

### a. URL Prefix Fix
- [x] `ResumeController.java`: đổi `@RequestMapping("/v1/resumes")` → `"/api/v1/resumes"`
- [x] `ResumeControllerTest.java`: cập nhật tất cả URL strings cho đúng prefix

### b. Port Fix
- [x] `application.yml`: đổi `${SERVER_PORT:8080}` → `${SERVER_PORT:8082}`

### c. User ID từ Header
- [x] Xóa field `userId` khỏi `dto/CreateResumeRequest.java`
- [x] `ResumeController`: thêm `@RequestHeader("X-User-Id") String userId` vào tất cả endpoints
- [x] `exception/ErrorCode.java`: thêm `COM_004` (MISSING_REQUIRED_HEADER)
- [x] `exception/GlobalExceptionHandler.java`: xử lý `MissingRequestHeaderException` → 400 với COM_004
- [x] `service/impl/ResumeServiceImpl.java`: cập nhật signature `create(String userId, CreateResumeRequest request)`
- [x] Cập nhật tất cả unit tests và controller tests liên quan

### d. ResumeStatus Enum
- [x] `entity/enums/ResumeStatus.java`: đổi `PROCESSING` → `PARSING`
- [x] `entity/enums/ResumeStatus.java`: thêm `UPLOADED`
- [x] `entity/enums/ResumeStatus.java`: thêm `DELETED`
- [x] `V1__Initial_database_schema.sql`: không tạo thêm V2 — đã gộp trực tiếp vào V1
- [x] `service/impl/ResumeServiceImpl.java`: `create()` → set status = `UPLOADED`
- [x] `service/impl/ResumeServiceImpl.java`: `reprocess()` → set status = `PARSING`
- [x] Cập nhật tests liên quan đến status

### e. Thêm `raw_text` Field
- [x] `V1__Initial_database_schema.sql`: thêm `raw_text TEXT` trực tiếp vào `CREATE TABLE resumes` (không tạo V3)
- [x] `entity/Resume.java`: thêm field `private String rawText;`

### f. Cleanup
- [x] Xóa file `src/main/java/.../repository/ResumeSkillProfileRepository.java~`
- [x] Review `dto/ParseJobRequest.java` (untracked) — giữ lại, dùng ở STEP 1
- [x] Review `dto/ParseJobResponse.java` (untracked) — giữ lại, dùng ở STEP 1
- [x] Review `mapper/ParseJobMapper.java` (untracked) — giữ lại, dùng ở STEP 1

### Verification STEP 0
- [ ] `mvn clean compile` — không có lỗi
- [ ] `mvn test` — tất cả tests pass
- [ ] Start app: `mvn spring-boot:run` — khởi động thành công ở port 8082

---

## STEP 1 — ParseJob Service (Async Job Tracking)

> Tracking trạng thái xử lý CV bất đồng bộ.

### Repository
- [x] `repository/ResumeParseJobRepository.java`: thêm `findByResumeId(String resumeId)`
- [x] `repository/ResumeParseJobRepository.java`: thêm `findByStatus(JobStatus status)`

### DTOs
- [x] Kiểm tra / tạo `dto/ParseJobRequest.java` — fields: `resumeId`, `jobType`
- [x] Kiểm tra / tạo `dto/ParseJobResponse.java` — fields: `id`, `resumeId`, `status`, `jobType`, `progress`, `retryCount`, `errorMessage`, `startedAt`, `finishedAt`

### Mapper
- [x] Kiểm tra / tạo `mapper/ParseJobMapper.java` — methods: `toEntity`, `toResponse`

### Service
- [x] `service/ResumeParseJobService.java`: định nghĩa interface với 4 methods:
  - `create(String resumeId, JobType jobType)` → `ParseJobResponse`
  - `getById(String id)` → `ParseJobResponse`
  - `cancel(String id)` → `ParseJobResponse`
  - `updateStatus(String id, JobStatus status)` → void
- [x] `service/impl/ResumeParseJobServiceImpl.java`: implement 4 methods trên
  - `create`: check resume tồn tại → check không có job RUNNING → tạo job với status `QUEUED`
  - `cancel`: chỉ cancel khi status là `QUEUED` hoặc `RUNNING` → throw JOB_002 nếu không hợp lệ
  - `updateStatus`: internal method dùng bởi processing pipeline

### Error Codes
- [x] `exception/ErrorCode.java`: thêm `JOB_001` (Job not found)
- [x] `exception/ErrorCode.java`: thêm `JOB_002` (Cannot cancel job in current status)
- [x] `exception/ErrorCode.java`: thêm `JOB_003` (Job already running)

### Controller
- [x] `controller/ResumeParseJobController.java`: implement 3 endpoints
  - `POST /api/v1/parse-jobs` → 201 Created
  - `GET /api/v1/parse-jobs/{id}` → 200 OK
  - `PUT /api/v1/parse-jobs/{id}/cancel` → 200 OK

### Tests
- [x] `test/service/impl/ResumeParseJobServiceImplTest.java` — unit tests (10 cases)
- [x] `test/controller/ResumeParseJobControllerTest.java` — MockMvc tests (8 cases)

### Verification STEP 1
- [ ] `mvn test` — tất cả tests pass
- [ ] `POST /api/v1/parse-jobs` → 201 với job status QUEUED
- [ ] `GET /api/v1/parse-jobs/{id}` → 200 với đúng data
- [ ] `PUT /api/v1/parse-jobs/{id}/cancel` → 200 nếu QUEUED, 400 nếu SUCCEEDED

---

## STEP 2 — File Management (MinIO + Redis)

> Upload/download CV + enqueue async parse job.

### Dependencies
- [ ] `pom.xml`: thêm dependency `io.minio:minio:8.5.7`
- [ ] `pom.xml`: thêm dependency `spring-boot-starter-data-redis`

### Config
- [ ] `application.yml`: thêm MinIO config (`endpoint`, `access-key`, `secret-key`, `bucket-name`)
- [ ] `application.yml`: thêm Redis config (`host`, `port`)
- [ ] `application.yml`: thêm multipart config (`max-file-size: 5MB`, `max-request-size: 5MB`)
- [ ] `application.yml`: thêm `worker.pool-size: ${WORKER_POOL_SIZE:10}`

### Config Classes
- [ ] `config/MinioConfig.java`: tạo `MinioClient` bean
- [ ] `config/RedisConfig.java`: tạo `RedisTemplate<String, String>` bean với StringSerializer

### Storage Service
- [ ] `service/FileStorageService.java`: interface với 3 methods (`upload`, `download`, `delete`)
- [ ] `service/impl/MinioFileStorageServiceImpl.java`: implement MinIO upload/download/delete

### Redis Queue Service
- [ ] `service/RedisJobQueueService.java`: implement
  - `enqueue(String resumeId)` → `LPUSH resume:parse:queue resumeId`
  - `dequeue()` → `RPOP resume:parse:queue`

### DTOs & Mapper
- [ ] `dto/ResumeFileResponse.java` — fields: `id`, `resumeId`, `originalName`, `fileType`, `fileSizeBytes`, `objectKey`, `createdAt`
- [ ] `mapper/ResumeFileMapper.java` — `toResponse(ResumeFile)`

### Repository
- [ ] `repository/ResumeFileRepository.java`: thêm `findByResumeId(String resumeId)`

### Service
- [ ] `service/ResumeFileService.java`: interface với 4 methods
- [ ] `service/impl/ResumeFileServiceImpl.java`: implement
  - `upload`: validate MIME type (PDF/DOCX) → validate size ≤5MB → upload MinIO → lưu DB → `enqueue(resumeId)` vào Redis → update resume status = `UPLOADED`
  - `getByResumeId`: tìm theo resumeId → throw FILE_001 nếu không có
  - `download`: lấy objectKey từ DB → download từ MinIO → trả về `Resource`
  - `delete`: xóa file trên MinIO → xóa record DB

### Error Codes
- [ ] `exception/ErrorCode.java`: thêm `FILE_001` (File not found)
- [ ] `exception/ErrorCode.java`: thêm `FILE_002` (Invalid file type — chỉ PDF/DOCX)
- [ ] `exception/ErrorCode.java`: thêm `FILE_003` (File size exceeded 5MB)
- [ ] `exception/ErrorCode.java`: thêm `FILE_004` (File already exists for this resume)

### Controller
- [ ] `controller/ResumeFileController.java`: đọc `X-User-Id` header, implement 4 endpoints
  - `POST /api/v1/resumes/{resumeId}/files` → 201 Created
  - `GET /api/v1/resumes/{resumeId}/files` → 200 OK
  - `GET /api/v1/resumes/{resumeId}/files/download` → ResponseEntity<Resource>
  - `DELETE /api/v1/resumes/{resumeId}/files` → 200 OK

### Tests
- [ ] `test/service/impl/ResumeFileServiceImplTest.java`
- [ ] `test/controller/ResumeFileControllerTest.java`

### Verification STEP 2
- [ ] `mvn test` — tất cả tests pass
- [ ] Upload PDF ≤5MB → 201, file xuất hiện trong MinIO, DB có record
- [ ] Upload file >5MB → 400 FILE_003
- [ ] Upload file .txt → 400 FILE_002
- [ ] `GET .../files/download` → trả về đúng file bytes
- [ ] Redis: sau upload có resumeId trong queue `resume:parse:queue`

---

## STEP 3 — Contact Management (Full CRUD)

> Thông tin liên lạc trích xuất từ CV — pipeline write + manual edit.

### DTOs
- [ ] `dto/ResumeContactRequest.java` — fields: `fullName`, `email`, `phone`, `location`, `linkedinUrl`, `otherInfo`
  - Validation: `@Email` trên email, regex trên phone, `@Pattern` trên linkedinUrl
- [ ] `dto/ResumeContactResponse.java` — tất cả fields + audit (`createdAt`, `updatedAt`)

### Mapper
- [ ] `mapper/ResumeContactMapper.java` — `toEntity`, `toResponse`

### Repository
- [ ] `repository/ResumeContactRepository.java`: thêm `findByResumeId(String resumeId)`

### Service
- [ ] `service/ResumeContactService.java`: interface
  - `getByResumeId(String resumeId)` → `ResumeContactResponse`
  - `createOrUpdate(String resumeId, ResumeContactRequest request)` → `ResumeContactResponse`
- [ ] `service/impl/ResumeContactServiceImpl.java`:
  - Validate resume tồn tại trước (throw RES_001 nếu không có)
  - Nếu contact chưa có → INSERT; nếu đã có → UPDATE (upsert)

### Error Codes
- [ ] `exception/ErrorCode.java`: thêm `CONTACT_001` (Contact not found)
- [ ] `exception/ErrorCode.java`: thêm `CONTACT_002` (Invalid contact data)

### Controller
- [ ] `controller/ResumeContactController.java`: implement 2 endpoints
  - `GET /api/v1/resumes/{resumeId}/contact` → 200 OK
  - `PUT /api/v1/resumes/{resumeId}/contact` → 200 OK (upsert)

### Tests
- [ ] `test/service/impl/ResumeContactServiceImplTest.java`
- [ ] `test/controller/ResumeContactControllerTest.java`

### Verification STEP 3
- [ ] `mvn test` — pass
- [ ] `PUT .../contact` với email không hợp lệ → 400
- [ ] `PUT .../contact` lần đầu → INSERT, lần 2 → UPDATE (không tạo record mới)
- [ ] `GET .../contact` resume không tồn tại → 404

---

## STEP 4 — Experience Management (Full CRUD)

> Kinh nghiệm làm việc — pipeline write + manual edit.

### DTOs
- [ ] `dto/ResumeExperienceRequest.java` — fields: `company`, `title`, `startDate`, `endDate`, `description`, `isCurrent`
  - Validation: `startDate ≤ endDate`; nếu `isCurrent=true` thì `endDate` phải null
- [ ] `dto/ResumeExperienceResponse.java`

### Mapper
- [ ] `mapper/ResumeExperienceMapper.java` — `toEntity`, `toResponse`, `toResponseList`

### Repository
- [ ] `repository/ResumeExperienceRepository.java`: thêm `findByResumeId(String resumeId)`

### Service
- [ ] `service/ResumeExperienceService.java`: interface (`list`, `create`, `update`, `delete`)
- [ ] `service/impl/ResumeExperienceServiceImpl.java`:
  - Mọi method: validate resume tồn tại trước
  - `update`/`delete`: validate experience thuộc đúng resumeId

### Error Codes
- [ ] `exception/ErrorCode.java`: thêm `EXP_001` (Experience not found)
- [ ] `exception/ErrorCode.java`: thêm `EXP_002` (Invalid date range)
- [ ] `exception/ErrorCode.java`: thêm `EXP_003` (Experience does not belong to resume)

### Controller
- [ ] `controller/ResumeExperienceController.java`: implement 4 endpoints
  - `GET /api/v1/resumes/{resumeId}/experiences` → 200 OK
  - `POST /api/v1/resumes/{resumeId}/experiences` → 201 Created
  - `PUT /api/v1/resumes/{resumeId}/experiences/{id}` → 200 OK
  - `DELETE /api/v1/resumes/{resumeId}/experiences/{id}` → 200 OK

### Tests
- [ ] `test/service/impl/ResumeExperienceServiceImplTest.java`
- [ ] `test/controller/ResumeExperienceControllerTest.java`

### Verification STEP 4
- [ ] `mvn test` — pass
- [ ] `POST` với `startDate > endDate` → 400 EXP_002
- [ ] `POST` với `isCurrent=true` và `endDate` không null → 400
- [ ] `DELETE` experience không thuộc resume → 404 EXP_003

---

## STEP 5 — Education Management (Full CRUD)

> Học vấn — pipeline write + manual edit.

### DTOs
- [ ] `dto/ResumeEducationRequest.java` — fields: `school`, `degree`, `field`, `startYear`, `endYear`, `description`
  - Validation: `startYear ≤ endYear`, năm trong khoảng 1900–current_year
- [ ] `dto/ResumeEducationResponse.java`

### Mapper
- [ ] `mapper/ResumeEducationMapper.java` — `toEntity`, `toResponse`, `toResponseList`

### Repository
- [ ] `repository/ResumeEducationRepository.java`: thêm `findByResumeId(String resumeId)`

### Service
- [ ] `service/ResumeEducationService.java`: interface (`list`, `create`, `update`, `delete`)
- [ ] `service/impl/ResumeEducationServiceImpl.java`:
  - Mọi method: validate resume tồn tại trước
  - `update`/`delete`: validate education thuộc đúng resumeId

### Error Codes
- [ ] `exception/ErrorCode.java`: thêm `EDU_001` (Education not found)
- [ ] `exception/ErrorCode.java`: thêm `EDU_002` (Invalid year range)
- [ ] `exception/ErrorCode.java`: thêm `EDU_003` (Education does not belong to resume)

### Controller
- [ ] `controller/ResumeEducationController.java`: implement 4 endpoints
  - `GET /api/v1/resumes/{resumeId}/educations` → 200 OK
  - `POST /api/v1/resumes/{resumeId}/educations` → 201 Created
  - `PUT /api/v1/resumes/{resumeId}/educations/{id}` → 200 OK
  - `DELETE /api/v1/resumes/{resumeId}/educations/{id}` → 200 OK

### Tests
- [ ] `test/service/impl/ResumeEducationServiceImplTest.java`
- [ ] `test/controller/ResumeEducationControllerTest.java`

### Verification STEP 5
- [ ] `mvn test` — pass
- [ ] `POST` với `startYear > endYear` → 400 EDU_002
- [ ] `POST` với `startYear = 1800` → 400
- [ ] `DELETE` education không thuộc resume → 404

---

## STEP 6 — Skills (Catalog + Resume Skills + Skill Profile)

### 6A — Skill Catalog (Seed + Read-only)

- [ ] Tạo `src/main/resources/db/migration/V4__Seed_skills_data.sql`
  - Insert ~50 skills phổ biến: Java, Spring Boot, Python, Go, SQL, PostgreSQL, Redis, Docker, Kubernetes, React, TypeScript, Git, AWS, Linux, Kafka, MongoDB...
- [ ] `dto/SkillResponse.java` — fields: `id`, `name`, `category`, `type`, `description`
- [ ] `mapper/SkillMapper.java`
- [ ] `repository/SkillRepository.java`: thêm queries
  - `findByCategory(String category)`
  - `findByNameContainingIgnoreCase(String name)`
- [ ] `service/SkillService.java`: interface (`list`, `getById`, `search`)
- [ ] `service/impl/SkillServiceImpl.java`
- [ ] `exception/ErrorCode.java`: thêm `SKILL_001` (Skill not found)
- [ ] `controller/SkillController.java`: implement 3 endpoints (GET only, không có POST/PUT/DELETE)
  - `GET /api/v1/skills?category=` → 200 OK
  - `GET /api/v1/skills/{id}` → 200 OK
  - `GET /api/v1/skills/search?q=` → 200 OK

### 6B — Resume Skills (Full CRUD)

- [ ] `dto/ResumeSkillRequest.java` — fields: `skillId`, `proficiencyLevel`, `yearsExperience`, `confidenceScore`, `evidenceText`, `isPrimary`
- [ ] `dto/ResumeSkillResponse.java`
- [ ] `mapper/ResumeSkillMapper.java`
- [ ] `repository/ResumeSkillRepository.java`: thêm queries
  - `findByResumeId(String resumeId)`
  - `findByResumeIdAndSkillId(String resumeId, String skillId)`
  - `existsByResumeIdAndSkillId(String resumeId, String skillId)`
- [ ] `service/ResumeSkillService.java`: interface (`list`, `create`, `update`, `delete`)
- [ ] `service/impl/ResumeSkillServiceImpl.java`:
  - `create`: validate skill tồn tại trong catalog, validate resume tồn tại
  - `update`/`delete`: validate skill thuộc đúng resumeId
- [ ] `exception/ErrorCode.java`: thêm `RESUME_SKILL_001` (Resume skill not found)
- [ ] `exception/ErrorCode.java`: thêm `RESUME_SKILL_002` (Skill already added to resume)
- [ ] `controller/ResumeSkillController.java`: implement 4 endpoints
  - `GET /api/v1/resumes/{resumeId}/skills` → 200 OK
  - `POST /api/v1/resumes/{resumeId}/skills` → 201 Created
  - `PUT /api/v1/resumes/{resumeId}/skills/{id}` → 200 OK
  - `DELETE /api/v1/resumes/{resumeId}/skills/{id}` → 200 OK

### 6C — Skill Profile

- [ ] `dto/ResumeSkillProfileResponse.java` — fields: `resumeId`, `topSkills`, `yearsEstimated`, `seniority`, `summary`, `signals`, `generatedAt`
- [ ] `dto/UpdateResumeSkillProfileRequest.java` — fields: `summary`, `signals`
- [ ] `mapper/ResumeSkillProfileMapper.java`
- [ ] `service/ResumeSkillProfileService.java`: interface (`getByResumeId`, `generate`, `update`)
- [ ] `service/impl/ResumeSkillProfileServiceImpl.java`:
  - `generate` logic:
    - Lấy tất cả ResumeSkill của resumeId
    - Sort by `confidenceScore` DESC → `topSkills`
    - `yearsEstimated` = max `yearsExperience` của primary skills
    - `seniority`: `<2yr=JUNIOR`, `2-5yr=MID`, `5-8yr=SENIOR`, `8+yr=PRINCIPAL`
    - Upsert ResumeSkillProfile
- [ ] `controller/ResumeSkillProfileController.java`: implement 3 endpoints
  - `GET /api/v1/resumes/{resumeId}/skill-profile` → 200 OK
  - `POST /api/v1/resumes/{resumeId}/skill-profile/generate` → 200 OK
  - `PUT /api/v1/resumes/{resumeId}/skill-profile` → 200 OK

### Tests STEP 6
- [ ] `test/service/impl/SkillServiceImplTest.java`
- [ ] `test/controller/SkillControllerTest.java`
- [ ] `test/service/impl/ResumeSkillServiceImplTest.java`
- [ ] `test/controller/ResumeSkillControllerTest.java`
- [ ] `test/service/impl/ResumeSkillProfileServiceImplTest.java`
- [ ] `test/controller/ResumeSkillProfileControllerTest.java`

### Verification STEP 6
- [ ] `mvn test` — pass
- [ ] Skill catalog seed đúng: `GET /api/v1/skills` trả về ~50 skills
- [ ] `GET /api/v1/skills/search?q=java` → trả về "Java"
- [ ] `POST .../skills` với skillId không tồn tại → 404
- [ ] `POST .../skills` với skillId đã thêm → 409 RESUME_SKILL_002
- [ ] `POST .../skill-profile/generate` → profile được tạo với seniority đúng
- [ ] `PUT /api/v1/skills/{id}` → 405 Method Not Allowed (read-only catalog)

---

## STEP 7 — Content Extraction Engine (Async Worker Pool)

> Core của service: pipeline xử lý CV bất đồng bộ.

### Dependencies
- [ ] `pom.xml`: thêm `org.apache.pdfbox:pdfbox:3.0.1`
- [ ] `pom.xml`: thêm `org.apache.poi:poi-ooxml:5.2.5`

### Async Config
- [ ] `config/AsyncConfig.java`: tạo `ThreadPoolTaskExecutor` bean tên `"workerPool"`
  - `corePoolSize` = `${worker.pool-size:10}`
  - `maxPoolSize` = `${worker.pool-size:10}`
  - `queueCapacity` = 100
  - `threadNamePrefix` = `"resume-worker-"`
- [ ] `IntellihireresumeApplication.java`: thêm `@EnableAsync`

### Text Extraction
- [ ] `service/TextExtractionService.java`: interface
  - `String extractFromPdf(byte[] fileBytes)`
  - `String extractFromDocx(byte[] fileBytes)`
- [ ] `service/impl/TextExtractionServiceImpl.java`:
  - PDF: dùng `PDDocument` + `PDFTextStripper` (PDFBox)
  - DOCX: dùng `XWPFDocument` + `XWPFWordExtractor` (POI)

### Worker Service
- [ ] `service/ResumeWorkerService.java`: `@Async("workerPool")` method
  - `processJob(String resumeId)` — pipeline:
    1. Update `ResumeParseJob.status` → `RUNNING`, `Resume.status` → `PARSING`
    2. Download file từ MinIO qua `FileStorageService.download(objectKey)`
    3. Detect file type (PDF/DOCX) từ `ResumeFile.fileType`
    4. Extract raw text → `TextExtractionService`
    5. Lưu `rawText` vào `resumes.raw_text`
    6. Parse contact → tạo/update `ResumeContact`
    7. Parse experiences → tạo `ResumeExperience` records
    8. Parse educations → tạo `ResumeEducation` records
    9. Extract skill keywords → match với `SkillRepository` → tạo `ResumeSkill` records
    10. Call `ResumeSkillProfileService.generate(resumeId)`
    11. Update `ResumeParseJob.status` → `SUCCEEDED`, `Resume.status` → `COMPLETED`
    - Nếu exception bất kỳ: status → `FAILED`, lưu `errorMessage`, tăng `retryCount`

### Queue Scheduler
- [ ] `service/ResumeQueueScheduler.java`: `@Scheduled(fixedDelay = 5000)`
  - `pollAndDispatch()`: gọi `RedisJobQueueService.dequeue()` → nếu có resumeId → gọi `ResumeWorkerService.processJob(resumeId)`
- [ ] `IntellihireresumeApplication.java`: thêm `@EnableScheduling`

### Update Reprocess
- [ ] `service/impl/ResumeServiceImpl.java`: method `reprocess()`:
  - Update resume status → `PARSING`
  - Tạo ParseJob mới với status `QUEUED`
  - `RedisJobQueueService.enqueue(resumeId)`

### Tests
- [ ] `test/service/impl/TextExtractionServiceImplTest.java` — test với sample PDF/DOCX bytes
- [ ] `test/service/ResumeWorkerServiceTest.java` — mock tất cả dependencies, verify pipeline steps

### Verification STEP 7
- [ ] `mvn test` — pass
- [ ] Upload PDF → poll Redis sau 5s → worker bắt đầu xử lý → resume status = PARSING → sau xử lý = COMPLETED
- [ ] `GET /api/v1/resumes/{id}` → status = COMPLETED
- [ ] `GET /api/v1/resumes/{id}/contact` → có data được extract
- [ ] `GET /api/v1/resumes/{id}/skills` → có skills được match
- [ ] `GET /api/v1/resumes/{id}/skill-profile` → profile đã generate
- [ ] Upload file lỗi (không đọc được) → resume status = FAILED, job.errorMessage có thông tin

---

## STEP 8 — Production Readiness

### Header Auth Filter
- [ ] `filter/HeaderAuthFilter.java` (`extends OncePerRequestFilter`):
  - Nếu thiếu `X-User-Id` header → response 403 Forbidden với message rõ ràng
  - Nếu có → lưu vào `request.setAttribute("userId", ...)` và `request.setAttribute("userRole", ...)`
  - Nếu thiếu `X-User-Role` → default = `"ROLE_USER"`
- [ ] Đăng ký filter trong `config/WebConfig.java` hoặc `@Component` + `@Order`

### Caching
- [ ] `IntellihireresumeApplication.java`: thêm `@EnableCaching`
- [ ] `config/CacheConfig.java`: cấu hình RedisCacheManager
- [ ] `service/impl/SkillServiceImpl.java`:
  - `list()`: thêm `@Cacheable("skills:list")`
  - `getById()`: thêm `@Cacheable("skills:detail")`

### Docker
- [ ] `Dockerfile`: tạo multi-stage build
  ```
  Stage 1 (build): maven:3.9-eclipse-temurin-17 → mvn package
  Stage 2 (runtime): eclipse-temurin:17-jre → copy jar → EXPOSE 8082 → ENTRYPOINT
  ```
- [ ] `docker-compose.yml`: tạo với 4 services
  - `resume-service` (port 8082, depends_on: postgres, redis, minio)
  - `postgres` (image: postgres:16-alpine, volume, env: POSTGRES_DB=resumes_db)
  - `redis` (image: redis:7-alpine, port 6379)
  - `minio` (image: minio/minio, port 9000/9001, volume)
- [ ] `.env.example`: tạo file mẫu với tất cả env variables cần thiết
- [ ] Test: `docker-compose up` → tất cả services healthy

### Logging & Monitoring
- [ ] Tạo `filter/MdcLoggingFilter.java` (`OncePerRequestFilter`):
  - Gắn `requestId` = `UUID.randomUUID()` vào MDC
  - Gắn `userId` = giá trị `X-User-Id` header vào MDC
  - Clear MDC sau khi request kết thúc
- [ ] `application.yml`: cập nhật log pattern để in `requestId`, `userId`
- [ ] Thêm custom Actuator metrics:
  - Counter: số lượng file upload
  - Timer: thời gian parse CV

### Testing
- [ ] Thêm dependency `spring-boot-testcontainers` vào `pom.xml`
- [ ] Thêm dependency `testcontainers` (postgresql, redis, minio module) vào `pom.xml`
- [ ] `test/integration/ResumeUploadIntegrationTest.java`: test full flow upload → parse với real containers
- [ ] Performance test: script upload 50 file đồng thời, verify P95 < 10s

### Verification STEP 8
- [ ] `mvn test` — pass toàn bộ (unit + integration)
- [ ] `docker-compose up --build` → tất cả 4 containers healthy
- [ ] Request không có `X-User-Id` header → 403
- [ ] Request có header → logs in ra `requestId` và `userId`
- [ ] `GET /api/actuator/health` → `{"status": "UP"}`
- [ ] `GET /api/actuator/metrics` → có custom metrics
- [ ] `GET /api/v1/skills` lần 2 → từ Redis cache (log không in SQL query)
- [ ] Test coverage ≥ 60%: `mvn verify -Pcoverage` (nếu có JaCoCo plugin)

---

---

## STEP 9 — Angular Project Setup + Core Architecture

> Tạo nền tảng Angular frontend tương tác với tất cả 32 REST API của backend.

### Project Init
- [ ] Khởi tạo project: `ng new intelli-hire-frontend --standalone`
  - Thư mục: `intelli-hire-frontend/` tại root repo
- [ ] Cài Angular Material: `ng add @angular/material`
- [ ] Cấu hình lazy-loaded routing (`app.routes.ts`)

### HTTP & Interceptors
- [ ] `core/interceptors/auth.interceptor.ts`: tự động gắn header `X-User-Id` + `X-User-Role` vào mọi request (lấy từ localStorage)
- [ ] `core/interceptors/error.interceptor.ts`: bắt HTTP 4xx/5xx → hiển thị snackbar message

### Environment Config
- [ ] `environments/environment.ts`: `apiUrl: 'http://localhost:8082'`
- [ ] `environments/environment.prod.ts`: `apiUrl` lấy từ env variable (NGINX inject)

### Core Services
- [ ] `core/services/api.service.ts`: base service với generic `get`, `post`, `put`, `delete` methods

### Shared Components
- [ ] `shared/components/loading-spinner/` — overlay loading indicator
- [ ] `shared/components/error-alert/` — hiển thị lỗi API dạng snackbar
- [ ] `shared/components/confirm-dialog/` — dialog xác nhận trước khi xóa

### Verification STEP 9
- [ ] `ng serve` → app khởi động tại port 4200 không lỗi
- [ ] Mọi HTTP request → header `X-User-Id` được gắn tự động (kiểm tra Network tab)
- [ ] Gọi endpoint sai → snackbar lỗi hiển thị đúng message

---

## STEP 10 — Resume & ParseJob UI

### Resume Feature (`features/resume/`)
- [ ] `services/resume.service.ts`: gọi đầy đủ `/api/v1/resumes` endpoints (list, create, getById, update, delete, reprocess)
- [ ] `pages/resume-list/`: bảng danh sách (title, status badge, createdAt), phân trang, nút Create/Delete
- [ ] `pages/resume-create/`: reactive form tạo resume với validation
- [ ] `pages/resume-detail/`: xem chi tiết + tabs (File, Contact, Experience, Education, Skills, Skill Profile)
- [ ] `pages/resume-edit/`: form edit title/description
- [ ] `components/resume-status-badge/`: chip màu theo `ResumeStatus`
  - `UPLOADED` = xanh dương, `PARSING` = cam, `COMPLETED` = xanh lá, `FAILED` = đỏ

### ParseJob Feature (`features/parse-job/`)
- [ ] `services/parse-job.service.ts`: gọi `POST /api/v1/parse-jobs`, `GET .../cancel`
- [ ] `components/parse-job-status/`: hiển thị status chip + progress bar
- [ ] Polling: sau upload → `interval(3000)` poll `GET /api/v1/parse-jobs/{id}` đến khi `SUCCEEDED` hoặc `FAILED` → unsubscribe

### Verification STEP 10
- [ ] `GET /api/v1/resumes` → danh sách hiển thị đầy đủ các cột
- [ ] Tạo resume → redirect sang trang detail
- [ ] Resume status badge đổi màu theo giá trị từ server
- [ ] ParseJob polling: sau 3s poll một lần, dừng khi job hoàn thành

---

## STEP 11 — File Upload + Contact / Experience / Education UI

### File Upload (`features/file/`)
- [ ] `services/resume-file.service.ts`: gọi upload, download, delete, getByResumeId
- [ ] `components/file-upload/`: drag & drop zone (Angular CDK)
  - Validate MIME type client-side (PDF/DOCX) → báo lỗi trước khi gọi API
  - Validate size ≤ 5MB client-side → báo lỗi ngay
  - Progress bar dùng `HttpRequest` + `reportProgress: true`
  - Sau upload thành công → tự động tạo ParseJob và bắt đầu polling

### Contact UI (`features/contact/`)
- [ ] `services/resume-contact.service.ts`: gọi `GET`/`PUT /api/v1/resumes/{id}/contact`
- [ ] `components/contact-form/`: reactive form upsert với validators
  - `@Email` validator cho email
  - Regex validator cho phone (VD: `^[+]?[\d\s\-]{7,15}$`)
  - URL pattern validator cho LinkedIn URL

### Experience UI (`features/experience/`)
- [ ] `services/resume-experience.service.ts`: gọi list, create, update, delete
- [ ] `components/experience-list/`: list với accordion expand panel
- [ ] `components/experience-form/` (dialog): DatePicker `startDate`/`endDate`, checkbox `isCurrent` → tự động disable `endDate`

### Education UI (`features/education/`)
- [ ] `services/resume-education.service.ts`: gọi list, create, update, delete
- [ ] `components/education-list/`: list với accordion
- [ ] `components/education-form/` (dialog): year input (number) + validation (1900 ≤ năm ≤ năm hiện tại)

### Verification STEP 11
- [ ] Upload PDF → progress bar chạy → polling ParseJob tự khởi động
- [ ] Upload file >5MB → lỗi hiển thị ngay, không gọi API
- [ ] Upload `.txt` → lỗi MIME type, không gọi API
- [ ] Contact form: email sai format → validator báo lỗi ngay tức thì
- [ ] Experience `isCurrent=true` → field `endDate` disabled

---

## STEP 12 — Skills & Skill Profile UI

### Skill Catalog (`features/skills/`)
- [ ] `services/skill.service.ts`: gọi `GET /api/v1/skills`, `GET /api/v1/skills/{id}`, `GET /api/v1/skills/search?q=`
- [ ] `components/skill-search/`: input với `debounceTime(300)` → gọi search API → autocomplete dropdown (Angular Material Autocomplete)

### Resume Skills (`features/resume-skills/`)
- [ ] `services/resume-skill.service.ts`: gọi list, create, update, delete
- [ ] `components/resume-skill-list/`: chip list hiển thị skills (tên, proficiency level, years)
- [ ] `components/add-skill-dialog/`: search catalog → chọn skill → nhập level/years → submit

### Skill Profile (`features/skill-profile/`)
- [ ] `services/resume-skill-profile.service.ts`: gọi get, generate, update
- [ ] `components/skill-profile-card/`:
  - Seniority badge: `JUNIOR` / `MID` / `SENIOR` / `PRINCIPAL`
  - Top skills bar chart (dùng `ng2-charts` hoặc Chart.js)
  - Summary text editable + signals list
  - Nút "Generate Profile" → `POST .../skill-profile/generate` → polling đến khi có kết quả

### Verification STEP 12
- [ ] Nhập "java" → autocomplete hiện "Java" trong dropdown
- [ ] Thêm skill đã có → server 409 → snackbar "Skill đã được thêm"
- [ ] Xóa skill → confirm dialog → xóa thành công → chip biến mất
- [ ] Generate profile → seniority badge cập nhật đúng (`JUNIOR`/`MID`/`SENIOR`/`PRINCIPAL`)

---

## STEP 13 — Frontend Production Readiness

### UX Improvements
- [ ] Skeleton loaders thay loading spinner cho tất cả list views
- [ ] Empty state components (hiển thị khi list trống, kèm nút action)
- [ ] Breadcrumb navigation (Resume List → Resume Detail → ...)
- [ ] Global error boundary component

### Auth Flow (Mock)
- [ ] `core/guards/auth.guard.ts`: kiểm tra `X-User-Id` trong localStorage → nếu thiếu, redirect `/login`
- [ ] `pages/login/`: form nhập User ID (demo/mock — thực tế nhận token từ API Gateway)

### Docker
- [ ] `intelli-hire-frontend/Dockerfile`: multi-stage build
  ```
  Stage 1 (build): node:20-alpine → npm ci → ng build --configuration=production
  Stage 2 (serve): nginx:alpine → copy dist → EXPOSE 80
  ```
- [ ] `intelli-hire-frontend/nginx.conf`: cấu hình SPA routing (`try_files $uri /index.html`)
- [ ] Cập nhật `docker-compose.yml`: thêm service `frontend` (port 80, `depends_on: resume-service`)

### Testing
- [ ] Unit tests (Karma/Jest):
  - `auth.interceptor.spec.ts`
  - `resume.service.spec.ts`
  - `resume-list.component.spec.ts`
- [ ] E2E (Playwright hoặc Cypress):
  - Test case 1: Upload CV → ParseJob polling → status COMPLETED
  - Test case 2: Tạo Resume → thêm Experience → xóa Experience
  - Test case 3: Generate Skill Profile → seniority hiển thị đúng

### Verification STEP 13
- [ ] `ng build --configuration=production` → thành công, bundle size < 2MB
- [ ] `docker-compose up --build` → container frontend healthy ở port 80
- [ ] Truy cập app không có localStorage `X-User-Id` → redirect `/login`
- [ ] E2E tests: cả 3 test cases pass
- [ ] Test coverage frontend ≥ 50%: `ng test --code-coverage`

---

## Tổng Kết

| Metric | Target | Kết Quả |
|--------|--------|---------|
| Endpoints (backend) | 32 | — |
| Test coverage (backend) | ≥ 60% | — |
| Concurrent uploads | 50 | — |
| Parse time P95 | < 10s | — |
| Response time (DB) | < 500ms | — |
| Angular version | 17+ (standalone) | — |
| UI Framework | Angular Material | — |
| State management | Services + BehaviorSubject | — |
| Bundle size (prod) | < 2MB | — |
| Test coverage (frontend) | ≥ 50% | — |
| E2E test cases | ≥ 3 | — |
