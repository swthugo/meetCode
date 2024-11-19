-- V1__init.sql

-- ==============================
-- Create 'problem' Table
-- ==============================
CREATE TABLE IF NOT EXISTS problem (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2550) NOT NULL,
    level VARCHAR(255) NOT NULL,
    visibility BOOLEAN NOT NULL,
    placeholder VARCHAR(2550) NOT NULL
);

-- ==============================
-- Create 'mc_user' Table
-- ==============================
CREATE TABLE IF NOT EXISTS mc_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    uid VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL
);

-- ==============================
-- Create 'solution' Table
-- ==============================
CREATE TABLE IF NOT EXISTS solution (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    problem_id BIGINT NOT NULL,
    answer TEXT NOT NULL,
    CONSTRAINT fk_solution_problem
        FOREIGN KEY(problem_id)
            REFERENCES problem(id)
            ON DELETE CASCADE
);

-- ==============================
-- Create 'submission' Table
-- ==============================
CREATE TABLE IF NOT EXISTS submission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    problem_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    result VARCHAR(255) NOT NULL,
    script VARCHAR(2550) NOT NULL,
    console TEXT NOT NULL,
    create_at TIMESTAMP,
    CONSTRAINT fk_submission_problem FOREIGN KEY (problem_id) REFERENCES problem(id) ON DELETE CASCADE,
    CONSTRAINT fk_submission_user FOREIGN KEY (user_id) REFERENCES mc_user(id) ON DELETE CASCADE
);

-- ==============================
-- Create 'test_case' Table
-- ==============================
CREATE TABLE IF NOT EXISTS test_case (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    problem_id BIGINT NOT NULL,
    testScript VARCHAR(2550) NOT NULL,
    CONSTRAINT fk_testCase_problem FOREIGN KEY (problem_id) REFERENCES problem(id) ON DELETE CASCADE
);

-- ==============================
-- Create 'user_problem' Table
-- ==============================
CREATE TABLE IF NOT EXISTS user_problem (
    problem_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    progress VARCHAR(255) NOT NULL,
    create_at TIMESTAMP,
    PRIMARY KEY (problem_id, user_id),
    CONSTRAINT fk_user_problem_problem FOREIGN KEY (problem_id) REFERENCES problem(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_problem_user FOREIGN KEY (user_id) REFERENCES mc_user(id) ON DELETE CASCADE
);