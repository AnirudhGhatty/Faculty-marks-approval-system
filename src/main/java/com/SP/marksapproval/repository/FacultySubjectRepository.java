
package com.SP.marksapproval.repository;

import com.SP.marksapproval.entity.FacultySubject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FacultySubjectRepository extends JpaRepository<FacultySubject, Integer> {

    // Find all subjects assigned to a particular faculty member
    List<FacultySubject> findByFaculty_FacultyId(Integer facultyId);

    // Find all faculty members assigned to a particular subject
    List<FacultySubject> findBySubject_SubjectId(Integer subjectId);

    // Check whether a faculty-subject assignment already exists
    boolean existsByFaculty_FacultyIdAndSubject_SubjectId(
            Integer facultyId,
            Integer subjectId
    );

    // Find assignments belonging to a specific faculty-subject pair
    List<FacultySubject> findByFaculty_FacultyIdAndSubject_SubjectId(
            Integer facultyId,
            Integer subjectId
    );
}
