-- =============================================
-- JOB PORTAL DATABASE SCHEMA
-- =============================================

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    company VARCHAR(200) NOT NULL,
    location VARCHAR(100) DEFAULT 'Remote',
    salary VARCHAR(50),
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    status VARCHAR(30) DEFAULT 'APPLIED',
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (job_id) REFERENCES jobs(id),
    UNIQUE(user_id, job_id)
);

CREATE TABLE IF NOT EXISTS questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_text VARCHAR(1000) NOT NULL,
    option_a VARCHAR(300) NOT NULL,
    option_b VARCHAR(300) NOT NULL,
    option_c VARCHAR(300) NOT NULL,
    option_d VARCHAR(300) NOT NULL,
    correct_answer CHAR(1) NOT NULL
);

CREATE TABLE IF NOT EXISTS test_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    question_ids VARCHAR(500) NOT NULL,
    status VARCHAR(20) DEFAULT 'IN_PROGRESS',
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    test_session_id BIGINT NOT NULL,
    score INT NOT NULL,
    total_questions INT NOT NULL,
    percentage DECIMAL(5,2),
    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (test_session_id) REFERENCES test_sessions(id)
);
