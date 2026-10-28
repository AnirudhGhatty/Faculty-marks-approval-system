
package com.SP.marksapproval.service;

import com.SP.marksapproval.entity.NcpMarks;
import com.SP.marksapproval.entity.Student;
import com.SP.marksapproval.entity.Subject;
import com.SP.marksapproval.repository.NcpMarksRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class NcpMarksService {

    private final NcpMarksRepository ncpMarksRepository;

    public NcpMarksService(NcpMarksRepository ncpMarksRepository) {
        this.ncpMarksRepository = ncpMarksRepository;
    }

    public List<NcpMarks> getNcpMarksBySubjectId(Integer subjectId) {
    return ncpMarksRepository.findBySubject_SubjectId(subjectId);
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

    /**
     * Saves and locks one NCP for a student and subject.
     * Other NCP scores and locks remain unchanged.
     */
    @Transactional
    public NcpMarks submitNcpMarks(
            Student student,
            Subject subject,
            String ncp,
            Integer score) {

        if (student == null || subject == null) {
            throw new IllegalArgumentException(
                    "Student and subject are required.");
        }

        if (score == null || score < 0 || score > 15) {
            throw new IllegalArgumentException(
                    "NCP marks must be between 0 and 15.");
        }

        if (ncp == null) {
            throw new IllegalArgumentException(
                    "An NCP assessment must be selected.");
        }

        String selectedNcp = ncp.trim().toUpperCase();

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

        switch (selectedNcp) {
            case "NCP1":
                if (Boolean.TRUE.equals(record.getNcp1Locked())) {
                    throw new IllegalStateException(
                            "NCP1 has already been submitted and locked.");
                }
                record.setNcp1(score);
                record.setNcp1Locked(true);
                break;

            case "NCP2":
                if (Boolean.TRUE.equals(record.getNcp2Locked())) {
                    throw new IllegalStateException(
                            "NCP2 has already been submitted and locked.");
                }
                record.setNcp2(score);
                record.setNcp2Locked(true);
                break;

            case "NCP3":
                if (Boolean.TRUE.equals(record.getNcp3Locked())) {
                    throw new IllegalStateException(
                            "NCP3 has already been submitted and locked.");
                }
                record.setNcp3(score);
                record.setNcp3Locked(true);
                break;

            default:
                throw new IllegalArgumentException(
                        "Invalid assessment. Select NCP1, NCP2 or NCP3.");
        }

        return ncpMarksRepository.save(record);
    }
}
