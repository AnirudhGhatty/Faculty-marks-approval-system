
package com.SP.marksapproval.repository;

import com.SP.marksapproval.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StudentRepository
        extends JpaRepository<Student, Integer> {

    Optional<Student> findByRollNo(String rollNo);

    List<Student>
    findByProgram_ProgramIdAndSemesterAndSectionIgnoreCaseOrderByRollNoAsc(
            Integer programId,
            Integer semester,
            String section
    );

    @Query("select distinct s.section from Student s order by s.section asc")
    List<String> findDistinctSections();
}
