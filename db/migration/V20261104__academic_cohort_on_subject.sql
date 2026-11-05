-- One-time migration for the current marks_approval schema.
-- Do not run until every NULL Class Of below has been replaced with the
-- confirmed graduating year for that subject. This script stops before any
-- schema change if mappings are incomplete or existing assignment data would
-- be changed by deriving Class Of from Subject.
--
-- Back up first, for example:
-- mysqldump --single-transaction --routines --triggers marks_approval > marks_approval-before-academic-cohort.sql

USE marks_approval;

CREATE TEMPORARY TABLE subject_class_of_mapping (
    subject_id INT NOT NULL PRIMARY KEY,
    class_of INT NULL
);

-- Current subject rows observed during the read-only schema audit.
-- Replace NULL values with verified mappings; do not guess.
INSERT INTO subject_class_of_mapping (subject_id, class_of) VALUES
    (2, NULL), -- BTech CSE / CS101 / semester 3 / Data Science and Artificial Intelligence
    (3, NULL), -- BTech CSE / CS001 / semester 3 / Data structures
    (4, NULL), -- BTech ECE / EC101 / semester 4 / Communications
    (6, NULL), -- BTech AIML / CS002 / semester 4 / Data structures sem4
    (7, NULL); -- BTech CSE / EC101 / semester 3 / Communications-3

DROP PROCEDURE IF EXISTS validate_academic_cohort_migration;
DELIMITER //
CREATE PROCEDURE validate_academic_cohort_migration()
BEGIN
    IF EXISTS (
        SELECT 1
        FROM subjects s
        LEFT JOIN subject_class_of_mapping m ON m.subject_id = s.subject_id
        WHERE m.subject_id IS NULL OR m.class_of IS NULL
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Migration stopped: supply a confirmed Class Of mapping for every subject.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM subject_class_of_mapping m
        LEFT JOIN subjects s ON s.subject_id = m.subject_id
        WHERE s.subject_id IS NULL
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Migration stopped: mapping contains an unknown subject_id.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM faculty_subject fs
        JOIN subject_class_of_mapping m ON m.subject_id = fs.subject_id
        WHERE fs.class_of <> m.class_of
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Migration stopped: an assignment Class Of differs from the supplied subject mapping; reconcile it first.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM faculty_subject
        GROUP BY faculty_id, subject_id, section
        HAVING COUNT(*) > 1
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Migration stopped: duplicate faculty, subject, and section assignments must be reconciled first.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM subjects s
        JOIN subject_class_of_mapping m ON m.subject_id = s.subject_id
        GROUP BY s.program_id, m.class_of, s.semester, s.course_code
        HAVING COUNT(*) > 1
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Migration stopped: subjects conflict on program, Class Of, semester, and course code.';
    END IF;
END//
DELIMITER ;

CALL validate_academic_cohort_migration();
DROP PROCEDURE validate_academic_cohort_migration;

ALTER TABLE subjects ADD COLUMN class_of INT NULL AFTER semester;

UPDATE subjects s
JOIN subject_class_of_mapping m ON m.subject_id = s.subject_id
SET s.class_of = m.class_of;

ALTER TABLE subjects MODIFY COLUMN class_of INT NOT NULL;

-- Course codes are unique within a program, graduating cohort, and semester.
ALTER TABLE subjects
    DROP INDEX uk_subject_program_semester_course,
    ADD CONSTRAINT uk_subject_program_class_semester_course
        UNIQUE (program_id, class_of, semester, course_code);

-- The audited schema uses uk_faculty_subject_cohort_section on
-- (faculty_id, subject_id, class_of, section).
ALTER TABLE faculty_subject
    DROP INDEX uk_faculty_subject_cohort_section,
    DROP COLUMN class_of,
    ADD CONSTRAINT uk_faculty_subject_section
        UNIQUE (faculty_id, subject_id, section);

-- Supports exact program, cohort, semester, and section roster lookups.
CREATE INDEX idx_students_academic_roster
    ON students (program_id, class_of, semester, section, roll_no);

DROP TEMPORARY TABLE subject_class_of_mapping;
