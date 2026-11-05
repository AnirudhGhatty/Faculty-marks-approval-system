# Academic cohort schema migration

`V20261104__academic_cohort_on_subject.sql` updates the existing MySQL schema to the current JPA model. It adds `subjects.class_of`, removes the old independent `faculty_subject.class_of`, changes assignment uniqueness to `(faculty_id, subject_id, section)`, scopes subject course-code uniqueness to `(program_id, class_of, semester, course_code)`, and adds an index for exact student-roster lookups.

## Before running

1. Take and verify a database backup. For example:

   ```powershell
   mysqldump --single-transaction --routines --triggers marks_approval > marks_approval-before-academic-cohort.sql
   ```

2. Open the SQL file and replace each `NULL` in `subject_class_of_mapping` with the confirmed graduating year for that subject. The current local database has five subjects (IDs 2, 3, 4, 6, and 7) and no student or faculty-assignment rows from which to infer those values. The script intentionally signals an error before changing the schema while any value is missing.
3. Run the script against a maintenance copy first, review the resulting schema and data, then run it against the intended database during a maintenance window. The SQL is for the audited existing schema and expects the current index names documented in the script.

The script also stops if assignment rows disagree with the supplied subject mapping, if mappings reference unknown subject IDs, or if the target uniqueness keys would collide. Resolve those data issues explicitly before rerunning it. Do not remove the preflight checks to force the migration through.

The migration is not executed by the application and was not run during development. Hibernate remains configured with `ddl-auto=validate`.
