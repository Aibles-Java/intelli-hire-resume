# Resume Service — intelli-hire-resume

Manages resume lifecycle: PDF/DOCX upload to MinIO, async AI parsing via configurable provider (OpenAI/Claude/Gemini/Grok/OpenRouter), structured data storage, skill profile generation, and CV review scoring.

## Run

```bash
cd intelli-hire-resume && ./mvnw spring-boot:run -Dspring.profiles.active=local
```

`local` profile is **required** — without it `bootstrap.yml` sets `fail-fast: true` and the service refuses to start.

## Vault Path

`secret/intellihireresume` (no dashes — non-standard) + `secret/shared`

## Conventions

- **Hand-written mappers only** — no MapStruct, no `mvnw clean compile` needed.
- AI parse is **async**: upload → `LPUSH resume:parse:queue` (Redis) → scheduler `RPOP` every 5 s → `workerPool` thread.
- `AiParsingServiceImpl.persistResults()` deletes all child rows before re-saving — safe to reprocess multiple times.

## Gotchas

- Vault path is `secret/intellihireresume` (no dashes). Using `secret/intelli-hire-resume` silently fails to load MinIO and AI credentials.
- Soft-delete: always use `findByIdActive()` / `findByUserIdActive()`. Plain `findById()` returns deleted records.
- `POST /api/v1/parse-jobs` does **NOT** enqueue the job — only upserts the DB record. Actual queuing happens on file upload or via `POST /resumes/{id}/reprocess`.

## Full Docs

@../.claude/docs/resume-service.md
