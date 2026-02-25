-- =====================================================
-- Migration V16: Add course time slots and backfill enrollment window defaults
-- =====================================================

-- Ensure enrollment window is not null for existing records (fallback to "open long window")
UPDATE courses
SET enrollment_start_date = COALESCE(enrollment_start_date, CURRENT_DATE),
    enrollment_end_date = COALESCE(enrollment_end_date, CURRENT_DATE + INTERVAL '365 days')
WHERE enrollment_start_date IS NULL OR enrollment_end_date IS NULL;

CREATE TABLE course_time_slots (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL,
    day_of_week SMALLINT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_course_time_slots_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT chk_course_time_slots_day CHECK (day_of_week BETWEEN 1 AND 7),
    CONSTRAINT chk_course_time_slots_time CHECK (start_time < end_time)
);

CREATE UNIQUE INDEX uk_course_time_slots_course_day_time
ON course_time_slots(course_id, day_of_week, start_time, end_time);

CREATE INDEX idx_course_time_slots_course_id ON course_time_slots(course_id);
CREATE INDEX idx_course_time_slots_day_of_week ON course_time_slots(day_of_week);

