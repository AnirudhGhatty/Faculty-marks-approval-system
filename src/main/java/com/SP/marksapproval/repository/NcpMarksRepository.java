
package com.SP.marksapproval.repository;

import com.SP.marksapproval.entity.NcpMarks;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NcpMarksRepository
        extends JpaRepository<NcpMarks, Integer> {

    Optional<NcpMarks> findByStudent_StudentIdAndSubject_SubjectId(
            Integer studentId,
            Integer subjectId);

    List<NcpMarks> findBySubject_SubjectId(Integer subjectId);
}
