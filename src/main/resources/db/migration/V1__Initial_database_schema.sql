-- =====================================================
-- Initial Database Schema for IntelliHire Resume System
-- =====================================================

-- Create skills table
CREATE TABLE skills (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(50),
    type VARCHAR(50),
    description TEXT,
    aliases JSON,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NOT NULL DEFAULT 'system',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(36)
);

-- Create indexes for skills table
CREATE INDEX idx_skills_name ON skills(name);
CREATE INDEX idx_skills_category_type ON skills(category, type);
CREATE INDEX idx_skills_is_active ON skills(is_active);

-- Create resumes table
CREATE TABLE resumes (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    title VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    raw_text TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NOT NULL DEFAULT 'system',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(36)
);

-- Create indexes for resumes table
CREATE INDEX idx_resumes_user_id ON resumes(user_id);
CREATE INDEX idx_resumes_status ON resumes(status);
CREATE INDEX idx_resumes_is_active ON resumes(is_active);
CREATE INDEX idx_resumes_user_status ON resumes(user_id, status, is_active);

-- Create resume_files table
CREATE TABLE resume_files (
    id VARCHAR(36) PRIMARY KEY,
    resume_id VARCHAR(36) NOT NULL UNIQUE,
    original_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(10) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    object_bucket VARCHAR(100) NOT NULL,
    object_key TEXT NOT NULL,
    object_etag VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NOT NULL DEFAULT 'system',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(36),
    CONSTRAINT fk_resume_files_resume FOREIGN KEY (resume_id) REFERENCES resumes(id)
);

-- Create indexes for resume_files table
CREATE INDEX idx_resume_files_resume_id ON resume_files(resume_id);
CREATE INDEX idx_resume_files_file_type ON resume_files(file_type);

-- Create resume_parse_jobs table
CREATE TABLE resume_parse_jobs (
    id VARCHAR(36) PRIMARY KEY,
    resume_id VARCHAR(36) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    job_type VARCHAR(50) NOT NULL,
    progress INT NOT NULL DEFAULT 0,
    retry_count INT NOT NULL DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NOT NULL DEFAULT 'system',
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(36),
    CONSTRAINT fk_resume_parse_jobs_resume FOREIGN KEY (resume_id) REFERENCES resumes(id)
);

-- Create indexes for resume_parse_jobs table
CREATE INDEX idx_resume_parse_jobs_status ON resume_parse_jobs(status);
CREATE INDEX idx_resume_parse_jobs_job_type ON resume_parse_jobs(job_type);
CREATE INDEX idx_resume_parse_jobs_created_at ON resume_parse_jobs(created_at);
CREATE INDEX idx_resume_parse_jobs_retry ON resume_parse_jobs(retry_count, status);

-- Create resume_skills table
CREATE TABLE resume_skills (
    id VARCHAR(36) PRIMARY KEY,
    resume_id VARCHAR(36) NOT NULL,
    skill_id VARCHAR(36) NOT NULL,
    proficiency_level VARCHAR(20),
    years_experience DECIMAL(4,1),
    is_estimated BOOLEAN NOT NULL DEFAULT false,
    confidence_score DECIMAL(3,2),
    evidence_text VARCHAR(500),
    is_primary BOOLEAN NOT NULL DEFAULT false,
    extracted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NOT NULL DEFAULT 'system',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(36),
    CONSTRAINT fk_resume_skills_resume FOREIGN KEY (resume_id) REFERENCES resumes(id),
    CONSTRAINT fk_resume_skills_skill FOREIGN KEY (skill_id) REFERENCES skills(id),
    CONSTRAINT uk_resume_skills_resume_skill UNIQUE (resume_id, skill_id)
);

-- Create indexes for resume_skills table
CREATE INDEX idx_resume_skills_resume_id ON resume_skills(resume_id);
CREATE INDEX idx_resume_skills_skill_id ON resume_skills(skill_id);
CREATE INDEX idx_resume_skills_confidence ON resume_skills(resume_id, confidence_score DESC);
CREATE INDEX idx_resume_skills_primary ON resume_skills(resume_id, is_primary);

-- Create resume_experiences table
CREATE TABLE resume_experiences (
    id VARCHAR(36) PRIMARY KEY,
    resume_id VARCHAR(36) NOT NULL,
    company VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    start_date DATE,
    end_date DATE,
    description TEXT,
    is_current BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NOT NULL DEFAULT 'system',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(36),
    CONSTRAINT fk_resume_experiences_resume FOREIGN KEY (resume_id) REFERENCES resumes(id)
);

-- Create indexes for resume_experiences table
CREATE INDEX idx_resume_experiences_resume_id ON resume_experiences(resume_id);
CREATE INDEX idx_resume_experiences_dates ON resume_experiences(resume_id, start_date DESC);
CREATE INDEX idx_resume_experiences_current ON resume_experiences(resume_id, is_current);

-- Create resume_educations table
CREATE TABLE resume_educations (
    id VARCHAR(36) PRIMARY KEY,
    resume_id VARCHAR(36) NOT NULL,
    school VARCHAR(255) NOT NULL,
    degree VARCHAR(255),
    field VARCHAR(255),
    start_year INT,
    end_year INT,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NOT NULL DEFAULT 'system',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(36),
    CONSTRAINT fk_resume_educations_resume FOREIGN KEY (resume_id) REFERENCES resumes(id)
);

-- Create indexes for resume_educations table
CREATE INDEX idx_resume_educations_resume_id ON resume_educations(resume_id);
CREATE INDEX idx_resume_educations_years ON resume_educations(resume_id, end_year DESC);

-- Create resume_contacts table
CREATE TABLE resume_contacts (
    id VARCHAR(36) PRIMARY KEY,
    resume_id VARCHAR(36) UNIQUE,
    full_name VARCHAR(255),
    email VARCHAR(100),
    phone VARCHAR(50),
    location VARCHAR(255),
    linkedin_url VARCHAR(255),
    other_info TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NOT NULL DEFAULT 'system',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(36),
    CONSTRAINT fk_resume_contacts_resume FOREIGN KEY (resume_id) REFERENCES resumes(id)
);

-- Create indexes for resume_contacts table
CREATE INDEX idx_resume_contacts_resume_id ON resume_contacts(resume_id);
CREATE INDEX idx_resume_contacts_email ON resume_contacts(email);

-- Create resume_skill_profiles table
CREATE TABLE resume_skill_profiles (
    id VARCHAR(36) PRIMARY KEY,
    resume_id VARCHAR(36) NOT NULL UNIQUE,
    top_skills JSON,
    years_estimated DECIMAL(5,2),
    seniority VARCHAR(20),
    summary TEXT,
    signals JSON,
    generated_at TIMESTAMP,
    generated_by VARCHAR(36),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NOT NULL DEFAULT 'system',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(36),
    CONSTRAINT fk_resume_skill_profiles_resume FOREIGN KEY (resume_id) REFERENCES resumes(id)
);

-- Create indexes for resume_skill_profiles table
CREATE INDEX idx_resume_skill_profiles_seniority ON resume_skill_profiles(seniority);
CREATE INDEX idx_resume_skill_profiles_years ON resume_skill_profiles(years_estimated);