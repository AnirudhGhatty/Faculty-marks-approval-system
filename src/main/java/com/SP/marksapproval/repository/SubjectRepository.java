
package com.SP.marksapproval.repository;

import com.SP.marksapproval.entity.Subject;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, Integer> {

    long countByProgram_ProgramId(Integer programId);

    List<Subject> findByProgram_ProgramIdAndSemester(
            Integer programId,
            Integer semester
    );

    List<Subject> findByProgram_ProgramIdAndClassOfAndSemester(
            Integer programId,
            Integer classOf,
            Integer semester
    );
}
