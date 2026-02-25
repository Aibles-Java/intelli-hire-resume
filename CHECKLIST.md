# IntelliHire Resume Service — Implementation Checklist

> Tham chiếu: `ROADMAP.md`
> Cập nhật theo tiến độ thực tế. Tick `[x]` khi hoàn thành.

---

## Tổng Tiến Độ

| Step | Nội Dung | Trạng Thái |
|------|----------|-----------|
| STEP 0 | Pre-work (fix base code) | ⬜ Chưa bắt đầu |
| STEP 1 | ParseJob Service + Controller | ⬜ Chưa bắt đầu |
| STEP 2 | File Management (MinIO + Redis) | ⬜ Chưa bắt đầu |
| STEP 3 | Contact Management | ⬜ Chưa bắt đầu |
| STEP 4 | Experience Management | ⬜ Chưa bắt đầu |
| STEP 5 | Education Management | ⬜ Chưa bắt đầu |
| STEP 6 | Skills (Catalog + CRUD + Profile) | ⬜ Chưa bắt đầu |
| STEP 7 | Content Extraction Engine | ⬜ Chưa bắt đầu |
| STEP 8 | Production Readiness | ⬜ Chưa bắt đầu |

---

## STEP 0 — Pre-work (Fix Base Code)

> Bắt buộc hoàn thành trước khi implement bất kỳ feature nào.

### a. URL Prefix Fix
- [ ] `ResumeController.java`: đổi `@RequestMapping("/v1/resumes")` → `"/api/v1/resumes"`
- [ ] `ResumeControllerTest.java`: cập nhật tất cả URL strings cho đúng prefix

### b. Port Fix
- [ ] `application.yml`: đổi `${SERVER_PORT:8080}` → `${SERVER_PORT:8082}`

### c. User ID từ Header
- [ ] Xóa field `userId` khỏi `dto/CreateResumeRequest.java`
- [ ] `ResumeController`: thêm `@RequestHeader("X-User-Id") String userId` vào tất cả endpoints
- [ ] `exception/ErrorCode.java`: thêm `COM_004` (MISSING_REQUIRED_HEADER)
- [ ] `exception/GlobalExceptionHandler.java`: xử lý `MissingRequestHeaderException` → 400 với COM_004
- [ ] `service/impl/ResumeServiceImpl.java`: cập nhật signature các method nhận userId từ tham số
- [ ] Cập nhật tất cả unit tests và controller tests liên quan

### d. ResumeStatus Enum
- [ ] `entity/enums/ResumeStatus.java`: đổi `PROCESSING` → `PARSING`
- [ ] `entity/enums/ResumeStatus.java`: thêm `UPLOADED`
- [ ] `entity/enums/ResumeStatus.java`: thêm `DELETED`
- [ ] Tạo `src/main/resources/db/migration/V2__Update_resume_status.sql`
  - Nội dung: `UPDATE resumes SET status = 'UPLOADED' WHERE status = 'PROCESSING';`
- [ ] `service/impl/ResumeServiceImpl.java`: `create()` → set status = `UPLOADED`
- [ ] `service/impl/ResumeServiceImpl.java`: `reprocess()` → set status = `PARSING`
- [ ] Cập nhật tests liên quan đến status

### e. Thêm `raw_text` Field
- [ ] Tạo `src/main/resources/db/migration/V3__Add_raw_text_to_resumes.sql`
  - Nội dung: `ALTER TABLE resumes ADD COLUMN raw_text TEXT;`
- [ ] `entity/Resume.java`: thêm field `private String rawText;`

### f. Cleanup
- [ ] Xóa file `src/main/java/.../repository/ResumeSkillProfileRepository.java~`
- [ ] Review `dto/ParseJobRequest.java` (untracked) — giữ lại nếu đúng pattern
- [ ] Review `dto/ParseJobResponse.java` (untracked) — giữ lại nếu đúng pattern
- [ ] Review `mapper/ParseJobMapper.java` (untracked) — giữ lại nếu đúng pattern

### Verification STEP 0
- [ ] `mvn clean compile` — không có lỗi
- [ ] `mvn test` — tất cả 56 tests pass (cập nhật nếu cần)
- [ ] Start app: `mvn spring-boot:run` — khởi động thành công ở port 8082

---

## STEP 1 — ParseJob Service (Async Job Tracking)

> Tracking trạng thái xử lý CV bất đồng bộ.

### Repository
- [ ] `repository/ResumeParseJobRepository.java`: thêm `findByResumeId(String resumeId)`
- [ ] `repository/ResumeParseJobRepository.java`: thêm `findByStatus(JobStatus status)`

### DTOs
- [ ] Kiểm tra / tạo `dto/ParseJobRequest.java` — fields: `resumeId`, `jobType`
- [ ] Kiểm tra / tạo `dto/ParseJobResponse.java` — fields: `id`, `resumeId`, `status`, `jobType`, `progress`, `retryCount`, `errorMessage`, `startedAt`, `finishedAt`

### Mapper
- [ ] Kiểm tra / tạo `mapper/ParseJobMapper.java` — methods: `toEntity`, `toResponse`

### Service
- [ ] `service/ResumeParseJobService.java`: định nghĩa interface với 4 methods:
  - `create(String resumeId, JobType jobType)` → `ParseJobResponse`
  - `getById(String id)` → `ParseJobResponse`
  - `cancel(String id)` → `ParseJobResponse`
  - `updateStatus(String id, JobStatus status)` → void
- [ ] `service/impl/ResumeParseJobServiceImpl.java`: implement 4 methods trên
  - `create`: check resume tồn tại → check không có job RUNNING → tạo job với status `QUEUED`
  - `cancel`: chỉ cancel khi status là `QUEUED` hoặc `RUNNING` → throw JOB_002 nếu không hợp lệ
  - `updateStatus`: internal method dùng bởi processing pipeline

### Error Codes
- [ ] `exception/ErrorCode.java`: thêm `JOB_001` (Job not found)
- [ ] `exception/ErrorCode.java`: thêm `JOB_002` (Cannot cancel job in current status)
- [ ] `exception/ErrorCode.java`: thêm `JOB_003` (Job already running)

### Controller
- [ ] `controller/ResumeParseJobController.java`: implement 3 endpoints
  - `POST /api/v1/parse-jobs` → 201 Created
  - `GET /api/v1/parse-jobs/{id}` → 200 OK
  - `PUT /api/v1/parse-jobs/{id}/cancel` → 200 OK

### Tests
- [ ] `test/service/impl/ResumeParseJobServiceImplTest.java` — unit tests
- [ ] `test/controller/ResumeParseJobControllerTest.java` — MockMvc tests

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

## Tổng Kết

| Metric | Target | Kết Quả |
|--------|--------|---------|
| Endpoints | 32 | — |
| Test coverage | ≥ 60% | — |
| Concurrent uploads | 50 | — |
| Parse time P95 | < 10s | — |
| Response time (DB) | < 500ms | — |
