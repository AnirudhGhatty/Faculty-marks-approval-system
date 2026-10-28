package com.SP.marksapproval.repository;

import com.SP.marksapproval.entity.FacultySubject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FacultySubjectRepository
        extends JpaRepository<FacultySubject, Integer> {

    List<FacultySubject> findByFaculty_FacultyId(Integer facultyId);

    List<FacultySubject> findBySubject_SubjectId(Integer subjectId);

    boolean existsByFaculty_FacultyIdAndSubject_SubjectIdAndSectionIgnoreCase(
            Integer facultyId,
            Integer subjectId,
            String section
    );

    List<FacultySubject> findByFaculty_FacultyIdAndSubject_SubjectId(
            Integer facultyId,
            Integer subjectId
    );
}
