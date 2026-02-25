# IntelliHire Resume - Full Project Roadmap

## Context

IntelliHire Resume là hệ thống xử lý và phân tích CV tự động cho nền tảng tuyển dụng. Dự án đang ở trạng thái:
- **Hoàn thành**: Database schema (9 bảng), exception handling, Resume CRUD (controller + service + repository), 56 test cases
- **Tồn tại nhưng chưa verified**: `dto/ParseJobRequest.java`, `dto/ParseJobResponse.java`, `mapper/ParseJobMapper.java` (untracked files, cần review)
- **Skeleton**: 8 service interfaces, 8 controller classes, các entity đầy đủ nhưng chưa có service logic
- **Chưa bắt đầu**: File processing, skill extraction, profile generation, security, production features
- **Cần fix trước**: `ResumeController` dùng `/v1/resumes` trong khi tất cả stub controllers dùng `/api/v1/...` → cần chuẩn hóa

Mục tiêu: Lên roadmap tổng thể từ Phase 2 đến Phase 7 theo thứ tự ưu tiên.

---

## Vị Trí Trong Hệ Thống IntelliHire (Microservices)

IntelliHire Resume **không phải standalone app** - đây là một microservice trong hệ thống lớn hơn:

```
API Gateway (Nginx)
├── /api/v1/auth/*       → Auth Service      (Java, port 8080)
├── /api/v1/questions/*  → Question Service  (Java, port 8081)
├── /api/v1/resumes/*    → Resume Service    (Java, port 8082) ← SERVICE NÀY
├── /api/v1/interviews/* → Interview Service (Java, port 8083)
├── /api/v1/reports/*    → Report Service    (Java, port 8085)
```

**Quy tắc authentication:**
- JWT được validate tại **API Gateway** (không phải tại service này)
- Gateway inject headers: `X-User-Id`, `X-User-Role` vào mỗi request
- Service này **chỉ đọc headers** - **KHÔNG tự validate JWT**

**Infrastructure riêng:**
- Database: `resumes_db` (PostgreSQL, không share với service khác)
- File storage: MinIO (shared infrastructure)
- Job queue: Redis (shared infrastructure)

---

## Kiến Trúc Tổng Quan

```
Client
  │
  ▼
Controller Layer (REST API)
  │ @Valid, @RestController, BaseResponse<T>
  ▼
Service Layer (Business Logic)
  │ @Transactional, Validation, Orchestration
  ▼
Repository Layer (Data Access)
  │ JpaRepository, Custom JPQL queries
  ▼
PostgreSQL (via Flyway migrations)
```

**Cross-cutting concerns:**
- `GlobalExceptionHandler` → xử lý tập trung tất cả exceptions
- `BaseResponse<T>` → chuẩn hóa response format (success/error/data)
- `BaseEntity` → audit fields tự động (createdAt, createdBy, updatedAt, updatedBy)
- `ResumeMapper` → pattern chuyển đổi Entity ↔ DTO (cần nhân rộng cho các entity khác)

---

## Files Quan Trọng (Existing)

| File | Vai trò |
|------|---------|
| `src/main/java/.../entity/*.java` | 9 entity classes (đầy đủ) |
| `src/main/java/.../exception/GlobalExceptionHandler.java` | Xử lý exception tập trung |
| `src/main/java/.../exception/ErrorCode.java` | Error codes (cần thêm) |
| `src/main/java/.../dto/BaseResponse.java` | Response wrapper |
| `src/main/java/.../mapper/ResumeMapper.java` | Pattern mapper mẫu |
| `src/main/java/.../service/impl/ResumeServiceImpl.java` | Service mẫu |
| `src/main/java/.../controller/ResumeController.java` | Controller mẫu |
| `src/main/resources/db/migration/V1__Initial_database_schema.sql` | Schema đầy đủ |

---

## Tổng Quan Resume Flow (End-to-End)

```
[Client]  POST /api/v1/resumes              → Tạo resume record (status: UPLOADED)
[Client]  POST /api/v1/resumes/{id}/files   → Upload CV to MinIO → enqueue parse job
[Async]   Worker Pool (10 threads)
              ↓ Dequeue from Redis
              ↓ Download file từ MinIO
              ↓ Extract raw text (PDF/DOCX)
              ↓ Parse → save contact/exp/edu/skills
              ↓ Match skills với seed catalog
              ↓ Generate skill profile
              ↓ Update status: COMPLETED / FAILED
[Client]  GET /api/v1/resumes/{id}           → Check status
[Client]  GET /api/v1/resumes/{id}/contact   → Read/Edit extracted contact
[Client]  GET /api/v1/resumes/{id}/experiences → Read/Edit experiences (Full CRUD)
[Client]  GET /api/v1/resumes/{id}/skill-profile → View aggregated skills
```

---

## Roadmap Implementation

### STEP 0: Pre-work (FIX BASE CODE TRƯỚC KHI BẮT ĐẦU)

**a. URL Prefix Fix:**
- [ ] `ResumeController.java`: `@RequestMapping("/v1/resumes")` → `"/api/v1/resumes"`
- [ ] `ResumeControllerTest.java`: cập nhật tất cả URL strings

**b. Port Fix (microservice):**
- [ ] `application.yml`: `${SERVER_PORT:8080}` → `${SERVER_PORT:8082}`

**c. User ID từ Header (không phải request body):**
- [ ] Bỏ field `userId` khỏi `dto/CreateResumeRequest.java`
- [ ] `ResumeController`: thêm `@RequestHeader("X-User-Id") String userId` vào mỗi endpoint
- [ ] Thêm `ErrorCode.COM_004` (MISSING_REQUIRED_HEADER) → xử lý `MissingRequestHeaderException` trong `GlobalExceptionHandler`
- [ ] Cập nhật `ResumeServiceImpl` signature và tests

**d. ResumeStatus Enum:**
- [ ] Cập nhật `entity/enums/ResumeStatus.java`:
  - Đổi `PROCESSING` → `PARSING`
  - Thêm `UPLOADED` (sau upload, trước khi parse)
  - Thêm `DELETED` (soft delete via status)
- [ ] Tạo `V2__Update_resume_status.sql`:
  ```sql
  UPDATE resumes SET status = 'UPLOADED' WHERE status = 'PROCESSING';
  ```
- [ ] `ResumeServiceImpl.create()`: status = `UPLOADED`
- [ ] `ResumeServiceImpl.reprocess()`: status = `PARSING`

**e. raw_text field:**
- [ ] Tạo `V3__Add_raw_text_to_resumes.sql`:
  ```sql
  ALTER TABLE resumes ADD COLUMN raw_text TEXT;
  ```
- [ ] Thêm field `rawText: String` vào entity `Resume.java`

**f. Cleanup:**
- [ ] Xóa `repository/ResumeSkillProfileRepository.java~`
- [ ] Review 3 untracked files: `ParseJobRequest.java`, `ParseJobResponse.java`, `ParseJobMapper.java`

**Verification:** `mvn test` - tất cả 56 tests pass

---

### STEP 1: ParseJob Service (Async Job Tracking)

*Tracking trạng thái xử lý CV bất đồng bộ*

**Files cần tạo/sửa:**
- `service/ResumeParseJobService.java` (interface): `create`, `getById`, `cancel`, `updateStatus`
- `service/impl/ResumeParseJobServiceImpl.java`
- `repository/ResumeParseJobRepository.java`: thêm `findByResumeId`, `findByStatus`
- `dto/ParseJobRequest.java` *(review untracked)*, `dto/ParseJobResponse.java` *(review untracked)*
- `mapper/ParseJobMapper.java` *(review untracked)*
- `controller/ResumeParseJobController.java`:
  - `POST /api/v1/parse-jobs`
  - `GET /api/v1/parse-jobs/{id}`
  - `PUT /api/v1/parse-jobs/{id}/cancel`
- `exception/ErrorCode.java`: thêm JOB_001, JOB_002, JOB_003

**Business logic:**
- `create`: check resume tồn tại + không có job đang RUNNING → tạo với status `QUEUED`
- `cancel`: chỉ cancel khi `QUEUED` hoặc `RUNNING`

---

### STEP 2: File Management (MinIO + Redis Queue)

*Upload/download CV + trigger async parse*

**Dependencies (`pom.xml`):**
```xml
<dependency>
  <groupId>io.minio</groupId>
  <artifactId>minio</artifactId>
  <version>8.5.7</version>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

**Config (`application.yml`):**
```yaml
minio:
  endpoint: ${MINIO_ENDPOINT:http://localhost:9000}
  access-key: ${MINIO_ACCESS_KEY:minioadmin}
  secret-key: ${MINIO_SECRET_KEY:minioadmin}
  bucket-name: ${MINIO_BUCKET:resume-files}
spring:
  data.redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
  servlet.multipart:
    max-file-size: 5MB
    max-request-size: 5MB
worker:
  pool-size: ${WORKER_POOL_SIZE:10}
```

**Files cần tạo:**
- `config/MinioConfig.java` → MinioClient bean
- `config/RedisConfig.java` → RedisTemplate<String, String> bean
- `service/FileStorageService.java` (interface): `upload`, `download`, `delete`
- `service/impl/MinioFileStorageServiceImpl.java`
- `service/RedisJobQueueService.java`: `enqueue(resumeId)`, `dequeue()` (Redis LIST `resume:parse:queue`)
- `dto/ResumeFileResponse.java`
- `mapper/ResumeFileMapper.java`
- `service/ResumeFileService.java`: `upload`, `getByResumeId`, `download`, `delete`
- `service/impl/ResumeFileServiceImpl.java`:
  - upload flow: validate (MIME PDF/DOCX + size ≤5MB) → MinIO → DB → enqueue Redis job
- `repository/ResumeFileRepository.java`: `findByResumeId`
- `controller/ResumeFileController.java` (header: `X-User-Id`):
  - `POST /api/v1/resumes/{resumeId}/files`
  - `GET /api/v1/resumes/{resumeId}/files`
  - `GET /api/v1/resumes/{resumeId}/files/download`
  - `DELETE /api/v1/resumes/{resumeId}/files`
- `exception/ErrorCode.java`: FILE_001, FILE_002, FILE_003, FILE_004

---

### STEP 3: Contact Management (Full CRUD)

*Thông tin liên lạc trích xuất từ CV - pipeline write + manual edit*

**Files cần tạo:**
- `dto/ResumeContactRequest.java` (fullName, email, phone, location, linkedinUrl, otherInfo)
- `dto/ResumeContactResponse.java`
- `mapper/ResumeContactMapper.java`
- `service/ResumeContactService.java`: `getByResumeId`, `createOrUpdate`
- `service/impl/ResumeContactServiceImpl.java`
- `repository/ResumeContactRepository.java`: `findByResumeId`
- `controller/ResumeContactController.java`:
  - `GET /api/v1/resumes/{resumeId}/contact`
  - `PUT /api/v1/resumes/{resumeId}/contact` (upsert)
- `exception/ErrorCode.java`: CONTACT_001, CONTACT_002
- Validation: `@Email`, phone regex, LinkedIn URL format

---

### STEP 4: Experience Management (Full CRUD)

*Kinh nghiệm làm việc - pipeline write + manual edit*

**Files cần tạo:**
- `dto/ResumeExperienceRequest.java` (company, title, startDate, endDate, description, isCurrent)
- `dto/ResumeExperienceResponse.java`
- `mapper/ResumeExperienceMapper.java`
- `service/ResumeExperienceService.java`: `list`, `create`, `update`, `delete`
- `service/impl/ResumeExperienceServiceImpl.java`
- `repository/ResumeExperienceRepository.java`: `findByResumeId`
- `controller/ResumeExperienceController.java`:
  - `GET /api/v1/resumes/{resumeId}/experiences`
  - `POST /api/v1/resumes/{resumeId}/experiences`
  - `PUT /api/v1/resumes/{resumeId}/experiences/{id}`
  - `DELETE /api/v1/resumes/{resumeId}/experiences/{id}`
- `exception/ErrorCode.java`: EXP_001, EXP_002, EXP_003
- Validation: `startDate ≤ endDate`; nếu `isCurrent=true` → `endDate` null

---

### STEP 5: Education Management (Full CRUD)

*Học vấn - pipeline write + manual edit*

**Files cần tạo:**
- `dto/ResumeEducationRequest.java` (school, degree, field, startYear, endYear, description)
- `dto/ResumeEducationResponse.java`
- `mapper/ResumeEducationMapper.java`
- `service/ResumeEducationService.java`: `list`, `create`, `update`, `delete`
- `service/impl/ResumeEducationServiceImpl.java`
- `repository/ResumeEducationRepository.java`: `findByResumeId`
- `controller/ResumeEducationController.java`:
  - `GET /api/v1/resumes/{resumeId}/educations`
  - `POST /api/v1/resumes/{resumeId}/educations`
  - `PUT /api/v1/resumes/{resumeId}/educations/{id}`
  - `DELETE /api/v1/resumes/{resumeId}/educations/{id}`
- `exception/ErrorCode.java`: EDU_001, EDU_002, EDU_003
- Validation: `startYear ≤ endYear`, năm 1900–current_year

---

### STEP 6: Skills (Seed Catalog + Resume Skills CRUD + Skill Profile)

#### 6A: Skill Catalog (Seed + Read-only)

> Không có admin CRUD. Skills được seed qua migration và match trong pipeline.

**Files cần tạo:**
- `V4__Seed_skills_data.sql`: insert ~50 skill phổ biến (Java, Spring Boot, Python, SQL, Docker, React...)
- `dto/SkillResponse.java`
- `mapper/SkillMapper.java`
- `service/SkillService.java`: `list(category)`, `getById`, `search(query)`
- `service/impl/SkillServiceImpl.java`
- `repository/SkillRepository.java`: `findByCategory`, `findByNameContainingIgnoreCase`
- `controller/SkillController.java` (**GET-only**, no POST/PUT/DELETE):
  - `GET /api/v1/skills?category=`
  - `GET /api/v1/skills/{id}`
  - `GET /api/v1/skills/search?q=`
- `exception/ErrorCode.java`: SKILL_001

#### 6B: Resume Skills (Full CRUD)

**Files cần tạo:**
- `dto/ResumeSkillRequest.java` (skillId, proficiencyLevel, yearsExperience, confidenceScore, evidenceText, isPrimary)
- `dto/ResumeSkillResponse.java`
- `mapper/ResumeSkillMapper.java`
- `service/ResumeSkillService.java`: `list`, `create`, `update`, `delete`
- `service/impl/ResumeSkillServiceImpl.java`
- `repository/ResumeSkillRepository.java`: `findByResumeId`, `findByResumeIdAndSkillId`, `existsByResumeIdAndSkillId`
- `controller/ResumeSkillController.java`:
  - `GET /api/v1/resumes/{resumeId}/skills`
  - `POST /api/v1/resumes/{resumeId}/skills`
  - `PUT /api/v1/resumes/{resumeId}/skills/{id}`
  - `DELETE /api/v1/resumes/{resumeId}/skills/{id}`
- `exception/ErrorCode.java`: RESUME_SKILL_001, RESUME_SKILL_002

#### 6C: Skill Profile

**Files cần tạo:**
- `dto/ResumeSkillProfileResponse.java` (topSkills, yearsEstimated, seniority, summary, signals, generatedAt)
- `dto/UpdateResumeSkillProfileRequest.java`
- `mapper/ResumeSkillProfileMapper.java`
- `service/ResumeSkillProfileService.java`: `getByResumeId`, `generate`, `update`
- `service/impl/ResumeSkillProfileServiceImpl.java`:
  - `generate`: sort ResumeSkills by confidenceScore → topSkills; calculate yearsEstimated; seniority rule: `<2yr=JUNIOR`, `2-5yr=MID`, `5-8yr=SENIOR`, `8+yr=PRINCIPAL`
- `controller/ResumeSkillProfileController.java`:
  - `GET /api/v1/resumes/{resumeId}/skill-profile`
  - `POST /api/v1/resumes/{resumeId}/skill-profile/generate`
  - `PUT /api/v1/resumes/{resumeId}/skill-profile`

---

### STEP 7: Content Extraction Engine (Async Worker Pool)

*Pipeline xử lý CV bất đồng bộ - core của service*

**Worker Pool Pattern (Java):**
```
Redis LIST "resume:parse:queue"
    ↓ @Scheduled poll mỗi 5s
ThreadPoolTaskExecutor (corePoolSize=10)
    ↓ @Async dispatch
ResumeWorkerService.processJob(resumeId):
    1. Update job → RUNNING, resume → PARSING
    2. Download file từ MinIO
    3. Extract raw text (PDF → PDFBox, DOCX → Apache POI)
    4. Save raw_text vào resumes table
    5. Regex/heuristic parse → contact, experiences, educations
    6. Match extracted skill keywords → ResumeSkill records
    7. Generate SkillProfile
    8. Update job → SUCCEEDED, resume → COMPLETED
    9. Lỗi bất kỳ → FAILED + lưu errorMessage + retryCount++
```

**Dependencies (`pom.xml`):**
```xml
<dependency>
  <groupId>org.apache.pdfbox</groupId>
  <artifactId>pdfbox</artifactId>
  <version>3.0.1</version>
</dependency>
<dependency>
  <groupId>org.apache.poi</groupId>
  <artifactId>poi-ooxml</artifactId>
  <version>5.2.5</version>
</dependency>
```

**Files cần tạo:**
- `config/AsyncConfig.java`: ThreadPoolTaskExecutor bean với `corePoolSize=${worker.pool-size:10}`
- `service/TextExtractionService.java`: `extractFromPdf(byte[])`, `extractFromDocx(byte[])`
- `service/impl/TextExtractionServiceImpl.java`
- `service/ResumeWorkerService.java` (`@Async("workerPool")`): pipeline logic như trên
- `service/ResumeQueueScheduler.java` (`@Scheduled(fixedDelay=5000)`): poll Redis → dispatch
- Cập nhật `ResumeService.reprocess()`: enqueue lại resumeId vào Redis queue

---

### STEP 8: Production Readiness

**Header Auth Filter:**
- [ ] `filter/HeaderAuthFilter.java` (`OncePerRequestFilter`):
  - Thiếu `X-User-Id` → 403 Forbidden
  - `X-User-Role` → default `ROLE_USER` nếu không có
  - Lưu vào `RequestContextHolder` để service layer đọc

**Caching (Redis đã setup từ STEP 2):**
- [ ] `@EnableCaching` trong Application class
- [ ] `@Cacheable("skills")` trên `SkillService.list()`, `getById()`

**Docker:**
- [ ] `Dockerfile` (multi-stage: build → runtime)
- [ ] `docker-compose.yml`:
  ```yaml
  services:
    resume-service:   # port 8082, depends on: postgres, redis, minio
    postgres:         # resumes_db
    redis:            # port 6379
    minio:            # port 9000/9001
  ```

**Logging & Monitoring:**
- [ ] MDC filter: gắn `requestId` (UUID), `userId` (từ X-User-Id) vào log context
- [ ] Micrometer + Actuator metrics đã có → thêm custom metrics (upload count, parse duration)

**Testing:**
- [ ] Integration tests với Testcontainers (PostgreSQL + Redis + MinIO)
- [ ] Performance test: verify 50 concurrent uploads ≤ 10s P95

---

## API Summary (32 Endpoints)

| Method | Path | Mô Tả | STEP |
|--------|------|--------|------|
| POST | `/api/v1/resumes` | Tạo resume | ✅ Done |
| GET | `/api/v1/resumes/{id}` | Xem status | ✅ Done |
| GET | `/api/v1/resumes?userId=X` | Danh sách | ✅ Done |
| PUT | `/api/v1/resumes/{id}` | Cập nhật title | ✅ Done |
| DELETE | `/api/v1/resumes/{id}` | Xóa (soft) | ✅ Done |
| POST | `/api/v1/resumes/{id}/reprocess` | Xử lý lại | ✅ Done |
| POST | `/api/v1/parse-jobs` | Tạo parse job | STEP 1 |
| GET | `/api/v1/parse-jobs/{id}` | Xem job status | STEP 1 |
| PUT | `/api/v1/parse-jobs/{id}/cancel` | Hủy job | STEP 1 |
| POST | `/api/v1/resumes/{id}/files` | Upload CV | STEP 2 |
| GET | `/api/v1/resumes/{id}/files` | File metadata | STEP 2 |
| GET | `/api/v1/resumes/{id}/files/download` | Download CV | STEP 2 |
| DELETE | `/api/v1/resumes/{id}/files` | Xóa file | STEP 2 |
| GET | `/api/v1/resumes/{id}/contact` | Xem contact | STEP 3 |
| PUT | `/api/v1/resumes/{id}/contact` | Sửa contact | STEP 3 |
| GET | `/api/v1/resumes/{id}/experiences` | Danh sách exp | STEP 4 |
| POST | `/api/v1/resumes/{id}/experiences` | Thêm exp | STEP 4 |
| PUT | `/api/v1/resumes/{id}/experiences/{eid}` | Sửa exp | STEP 4 |
| DELETE | `/api/v1/resumes/{id}/experiences/{eid}` | Xóa exp | STEP 4 |
| GET | `/api/v1/resumes/{id}/educations` | Danh sách edu | STEP 5 |
| POST | `/api/v1/resumes/{id}/educations` | Thêm edu | STEP 5 |
| PUT | `/api/v1/resumes/{id}/educations/{eid}` | Sửa edu | STEP 5 |
| DELETE | `/api/v1/resumes/{id}/educations/{eid}` | Xóa edu | STEP 5 |
| GET | `/api/v1/skills?category=` | Skill catalog list | STEP 6A |
| GET | `/api/v1/skills/{id}` | Skill chi tiết | STEP 6A |
| GET | `/api/v1/skills/search?q=` | Tìm skill | STEP 6A |
| GET | `/api/v1/resumes/{id}/skills` | Danh sách skills | STEP 6B |
| POST | `/api/v1/resumes/{id}/skills` | Thêm skill | STEP 6B |
| PUT | `/api/v1/resumes/{id}/skills/{sid}` | Sửa skill | STEP 6B |
| DELETE | `/api/v1/resumes/{id}/skills/{sid}` | Xóa skill | STEP 6B |
| GET | `/api/v1/resumes/{id}/skill-profile` | Xem profile | STEP 6C |
| POST | `/api/v1/resumes/{id}/skill-profile/generate` | Generate | STEP 6C |
| PUT | `/api/v1/resumes/{id}/skill-profile` | Sửa profile | STEP 6C |

---

## Thứ Tự Implement

| Step | Nội Dung | Depends On |
|------|----------|------------|
| STEP 0 | Pre-work: URL, port, userId header, enum, raw_text | — |
| STEP 1 | ParseJob Service + Controller | STEP 0 |
| STEP 2 | File Management (MinIO + Redis) | STEP 1 |
| STEP 3 | Contact Management | STEP 2 |
| STEP 4 | Experience Management | STEP 2 |
| STEP 5 | Education Management | STEP 2 |
| STEP 6 | Skills (Catalog + Resume Skills + Profile) | STEP 2 |
| STEP 7 | Content Extraction Engine | STEP 2 + 3 + 4 + 5 + 6 |
| STEP 8 | Production Readiness | STEP 7 |

---

## Pattern Tái Sử Dụng Cho Mỗi Domain Mới

Mỗi domain mới (Contact, Experience, Education, Skill) cần tạo theo pattern:
1. **DTO**: `Create{Entity}Request`, `Update{Entity}Request`, `{Entity}Response` với `@JsonNaming(SnakeCaseStrategy.class)`
2. **Mapper**: `{Entity}Mapper` với `@Component` (tương tự `ResumeMapper`)
3. **Service**: Interface + Impl với `@Transactional(rollbackFor = Exception.class)` và logging
4. **Controller**: `@RestController`, `@CrossOrigin("*")`, `BaseResponse<T>`, `@Valid`
5. **Repository**: Extends `JpaRepository<T, String>`, thêm custom `@Query` JPQL khi cần
6. **Tests**: Unit test cho service, controller test với MockMvc
7. **Error codes**: Thêm vào `ErrorCode.java`

**File mẫu tham chiếu:**
- Controller pattern: `controller/ResumeController.java`
- Service pattern: `service/impl/ResumeServiceImpl.java`
- Mapper pattern: `mapper/ResumeMapper.java`
- Repository pattern: `repository/ResumeRepository.java`

---

## Non-Functional Requirements (từ System Design Doc)

| Metric | Target |
|--------|--------|
| Concurrent uploads | 50 simultaneous |
| Parse time (P95) | < 10s per CV |
| Max file size | 5MB |
| Response time (DB query) | < 500ms |
| Test coverage | > 60% |
| Availability | 99.5% |
| Worker pool size | 10 threads (configurable) |

---

## Verification

Sau mỗi phase:
1. **Build**: `mvn clean compile` - không có lỗi compile
2. **Tests**: `mvn test` - tất cả tests pass
3. **Run**: `mvn spring-boot:run` - app khởi động với DB kết nối
4. **API Test**: Dùng curl hoặc Postman test từng endpoint
5. **Actuator**: `GET /api/actuator/health` → `{"status": "UP"}`
