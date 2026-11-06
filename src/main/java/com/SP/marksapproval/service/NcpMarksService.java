package com.SP.marksapproval.service;

import com.SP.marksapproval.entity.NcpMarks;
import com.SP.marksapproval.entity.Student;
import com.SP.marksapproval.entity.Subject;
import com.SP.marksapproval.repository.NcpMarksRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Service
public class NcpMarksService {

    private final NcpMarksRepository ncpMarksRepository;

    public NcpMarksService(NcpMarksRepository ncpMarksRepository) {
        this.ncpMarksRepository = ncpMarksRepository;
    }

    public List<NcpMarks> getNcpMarksBySubjectId(Integer subjectId) {
        return ncpMarksRepository.findBySubject_SubjectId(subjectId);
    }

    public List<NcpMarks> getNcpMarksByStudentId(Integer studentId) {
        return ncpMarksRepository.findByStudent_StudentId(studentId);
    }

    public List<NcpMarks> getAllNcpMarks() {
        return ncpMarksRepository.findAll();
    }

    public Optional<NcpMarks> getNcpMarksById(Integer id) {
        return ncpMarksRepository.findById(id);
    }

    public NcpMarks saveNcpMarks(NcpMarks ncpMarks) {
        return ncpMarksRepository.save(ncpMarks);
    }

    public void deleteNcpMarks(Integer id) {
        ncpMarksRepository.deleteById(id);
    }

    private int getMaximumMarks(String ncp) {

        if (ncp == null || ncp.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "An NCP assessment must be selected.");
        }

        switch (ncp.trim().toUpperCase()) {

            case "NCP1":
            case "NCP3":
                return 15;

            case "NCP2":
                return 30;

            default:
                throw new IllegalArgumentException(
                        "Invalid assessment. Select NCP1, NCP2 or NCP3.");
        }
    }

    /**
     * Validates that marks are within the assessment maximum
     * and contain no more than two meaningful decimal places.
     */
    private boolean isValidScore(BigDecimal score, int maximumMarks) {

        return score != null
                && score.compareTo(BigDecimal.ZERO) >= 0
                && score.compareTo(BigDecimal.valueOf(maximumMarks)) <= 0
                && score.stripTrailingZeros().scale() <= 2;
    }

    private boolean isLocked(NcpMarks record, String ncp) {

        switch (ncp) {

            case "NCP1":
                return Boolean.TRUE.equals(record.getNcp1Locked());

            case "NCP2":
                return Boolean.TRUE.equals(record.getNcp2Locked());

            case "NCP3":
                return Boolean.TRUE.equals(record.getNcp3Locked());

            default:
                return false;
        }
    }

    private void setScoreAndLock(
            NcpMarks record,
            String ncp,
            BigDecimal score) {

        switch (ncp) {

            case "NCP1":
                record.setNcp1(score);
                record.setNcp1Locked(true);
                break;

            case "NCP2":
                record.setNcp2(score);
                record.setNcp2Locked(true);
                break;

            case "NCP3":
                record.setNcp3(score);
                record.setNcp3Locked(true);
                break;

            default:
                throw new IllegalArgumentException("Invalid NCP.");
        }
    }

    /**
     * Saves an incomplete NCP marks draft without locking it.
     */
    @Transactional
    public void saveDraftBulkNcpMarks(
            List<Student> students,
            Subject subject,
            String ncp,
            Map<Integer, BigDecimal> scores) {

        if (students == null || students.isEmpty()) {
            throw new IllegalArgumentException(
                    "There are no students in this roster.");
        }

        if (subject == null || subject.getSubjectId() == null) {
            throw new IllegalArgumentException(
                    "A valid subject is required.");
        }

        if (scores == null) {
            throw new IllegalArgumentException(
                    "Student marks are required.");
        }

        String selectedNcp = ncp == null
                ? null
                : ncp.trim().toUpperCase();

        int maximumMarks = getMaximumMarks(selectedNcp);

        // Build a map of the students belonging to this roster.
        Map<Integer, Student> rosterStudents = new HashMap<>();

        for (Student student : students) {

            if (student == null || student.getStudentId() == null) {
                throw new IllegalArgumentException(
                        "The roster contains an invalid student.");
            }

            if (rosterStudents.put(
                    student.getStudentId(), student) != null) {
                throw new IllegalArgumentException(
                        "The roster contains duplicate students.");
            }
        }

        // Reject scores belonging to students outside this roster.
        if (!rosterStudents.keySet().containsAll(scores.keySet())) {
            throw new IllegalArgumentException(
                    "A submitted student does not belong to this roster.");
        }

        // Validate all non-empty marks before modifying any records.
        for (Map.Entry<Integer, BigDecimal> entry : scores.entrySet()) {

            BigDecimal score = entry.getValue();

            if (score != null && !isValidScore(score, maximumMarks)) {
                throw new IllegalArgumentException(
                        "Invalid marks for student "
                                + rosterStudents.get(entry.getKey()).getRollNo()
                                + ". Enter marks between 0 and "
                                + maximumMarks
                                + " with at most 2 decimal places.");
            }
        }

        Map<Integer, NcpMarks> recordsToSave = new HashMap<>();

        // Retrieve existing records and protect locked assessments.
        for (Map.Entry<Integer, BigDecimal> entry : scores.entrySet()) {

            Integer studentId = entry.getKey();
            Student student = rosterStudents.get(studentId);
            BigDecimal score = entry.getValue();

            Optional<NcpMarks> existing =
                    ncpMarksRepository
                            .findByStudent_StudentIdAndSubject_SubjectId(
                                    studentId,
                                    subject.getSubjectId());

            // No need to create an empty database record.
            if (existing.isEmpty() && score == null) {
                continue;
            }

            NcpMarks record;

            if (existing.isPresent()) {

                record = existing.get();

                if (isLocked(record, selectedNcp)) {
                    throw new IllegalStateException(
                            selectedNcp
                                    + " is already locked for student "
                                    + student.getRollNo() + ".");
                }

            } else {

                record = new NcpMarks();
                record.setStudent(student);
                record.setSubject(subject);
            }

            recordsToSave.put(studentId, record);
        }

        // Update only the selected NCP. Never change its lock flag.
        for (Map.Entry<Integer, BigDecimal> entry : scores.entrySet()) {

            NcpMarks record = recordsToSave.get(entry.getKey());

            if (record == null) {
                continue;
            }

            BigDecimal score = entry.getValue();

            switch (selectedNcp) {

                case "NCP1":
                    record.setNcp1(score);
                    break;

                case "NCP2":
                    record.setNcp2(score);
                    break;

                case "NCP3":
                    record.setNcp3(score);
                    break;

                default:
                    throw new IllegalArgumentException("Invalid NCP.");
            }
        }

        ncpMarksRepository.saveAll(recordsToSave.values());
    }

    /**
     * Submits marks for an entire subject roster.
     *
     * All students and scores are validated before records are modified.
     * All records are saved within one database transaction.
     */
    @Transactional
    public void submitBulkNcpMarks(
            List<Student> students,
            Subject subject,
            String ncp,
            Map<Integer, BigDecimal> scores) {

        // 1. Validate the roster and input.
        if (students == null || students.isEmpty()) {
            throw new IllegalArgumentException(
                    "There are no students in this roster.");
        }

        if (subject == null || subject.getSubjectId() == null) {
            throw new IllegalArgumentException(
                    "A valid subject is required.");
        }

        if (scores == null) {
            throw new IllegalArgumentException(
                    "Student marks are required.");
        }

        String selectedNcp = ncp == null
                ? null
                : ncp.trim().toUpperCase();

        int maximumMarks = getMaximumMarks(selectedNcp);

        // 2. Verify that the roster contains unique student IDs.
        Set<Integer> rosterIds = new HashSet<>();

        for (Student student : students) {

            if (student == null || student.getStudentId() == null) {
                throw new IllegalArgumentException(
                        "The roster contains an invalid student.");
            }

            if (!rosterIds.add(student.getStudentId())) {
                throw new IllegalArgumentException(
                        "The roster contains duplicate students.");
            }
        }

        // 3. Verify that scores exist for exactly the entire roster.
        if (!scores.keySet().equals(rosterIds)) {
            throw new IllegalArgumentException(
                    "Please submit marks for every student in the roster only.");
        }

        // 4. Validate every score before modifying any record.
        for (Student student : students) {

            BigDecimal score = scores.get(student.getStudentId());

            if (!isValidScore(score, maximumMarks)) {

                throw new IllegalArgumentException(
                        "Invalid marks for student "
                                + student.getRollNo()
                                + ". Enter marks between 0 and "
                                + maximumMarks
                                + " with at most 2 decimal places.");
            }
        }

        // 5. Retrieve existing records and check individual NCP locks.
        Map<Integer, NcpMarks> recordsToSave = new HashMap<>();

        for (Student student : students) {

            Integer studentId = student.getStudentId();

            Optional<NcpMarks> existing =
                    ncpMarksRepository
                            .findByStudent_StudentIdAndSubject_SubjectId(
                                    studentId,
                                    subject.getSubjectId());

            NcpMarks record;

            if (existing.isPresent()) {

                record = existing.get();

                if (isLocked(record, selectedNcp)) {
                    throw new IllegalStateException(
                            selectedNcp
                                    + " is already locked for student "
                                    + student.getRollNo() + ".");
                }

            } else {

                record = new NcpMarks();
                record.setStudent(student);
                record.setSubject(subject);
            }

            recordsToSave.put(studentId, record);
        }

        // 6. Update every record only after all validations pass.
        for (Student student : students) {

            NcpMarks record =
                    recordsToSave.get(student.getStudentId());

            setScoreAndLock(
                    record,
                    selectedNcp,
                    scores.get(student.getStudentId()));
        }

        // 7. Save the complete batch in one transaction.
        ncpMarksRepository.saveAll(recordsToSave.values());
    }

    /**
     * Submits marks for an individual student.
     * The selected assessment is automatically locked after submission.
     */
    @Transactional
    public NcpMarks submitNcpMarks(
            Student student,
            Subject subject,
            String ncp,
            BigDecimal score) {

        if (student == null || subject == null) {
            throw new IllegalArgumentException(
                    "Student and subject are required.");
        }

        if (student.getStudentId() == null
                || subject.getSubjectId() == null) {
            throw new IllegalArgumentException(
                    "Student and subject must have valid IDs.");
        }

        int maximumMarks = getMaximumMarks(ncp);
        String selectedNcp = ncp.trim().toUpperCase();

        if (!isValidScore(score, maximumMarks)) {
            throw new IllegalArgumentException(
                    "Marks must be between 0 and "
                            + maximumMarks
                            + " with at most 2 decimal places.");
        }

        NcpMarks record = ncpMarksRepository
                .findByStudent_StudentIdAndSubject_SubjectId(
                        student.getStudentId(),
                        subject.getSubjectId())
                .orElseGet(() -> {

                    NcpMarks newRecord = new NcpMarks();
                    newRecord.setStudent(student);
                    newRecord.setSubject(subject);

                    return newRecord;
                });

        if (isLocked(record, selectedNcp)) {
            throw new IllegalStateException(
                    selectedNcp
                            + " has already been submitted and locked.");
        }

        setScoreAndLock(record, selectedNcp, score);

        return ncpMarksRepository.save(record);
    }
}