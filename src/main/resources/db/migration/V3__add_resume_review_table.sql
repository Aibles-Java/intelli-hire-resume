-- =====================================================
-- V3: Add resume_reviews table for AI CV Review feature
-- =====================================================

CREATE TABLE IF NOT EXISTS resume_reviews (
    id               VARCHAR(36)   NOT NULL,
    resume_id        VARCHAR(36)   NOT NULL UNIQUE,
    overall_score    NUMERIC(3,1)  NOT NULL,
    sections         JSON          NOT NULL,
    strengths        JSON          NOT NULL DEFAULT '[]',
    weaknesses       JSON          NOT NULL DEFAULT '[]',
    priority_actions JSON          NOT NULL DEFAULT '[]',
    generated_at     TIMESTAMP     NOT NULL,
    created_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(36)   NOT NULL DEFAULT 'system',
    updated_at       TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(36),
    CONSTRAINT pk_resume_reviews PRIMARY KEY (id),
    CONSTRAINT fk_resume_reviews_resume FOREIGN KEY (resume_id)
        REFERENCES resumes(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_resume_reviews_resume_id ON resume_reviews(resume_id);
