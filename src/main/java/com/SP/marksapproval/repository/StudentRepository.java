package com.SP.marksapproval.repository;

import com.SP.marksapproval.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository
        extends JpaRepository<Student, Integer> {

    Optional<Student> findByRollNo(String rollNo);

    long countByProgram_ProgramId(Integer programId);

    // Existing retrieval method — retained for compatibility.
    List<Student>
    findByProgram_ProgramIdAndSemesterAndSectionIgnoreCaseOrderByRollNoAsc(
            Integer programId,
            Integer semester,
            String section
    );

    // Retrieve students belonging to an exact graduating cohort.
    List<Student>
    findByProgram_ProgramIdAndClassOfAndSemesterAndSectionIgnoreCaseOrderByRollNoAsc(
            Integer programId,
            Integer classOf,
            Integer semester,
            String section
    );

    // Get all distinct graduating years.
    @Query("SELECT DISTINCT s.classOf FROM Student s ORDER BY s.classOf DESC")
    List<Integer> findDistinctClassOf();

    // Get graduating years available within a selected program.
    @Query("""
            SELECT DISTINCT s.classOf
            FROM Student s
            WHERE s.program.programId = :programId
            ORDER BY s.classOf DESC
            """)
    List<Integer> findDistinctClassOfByProgram(
            @Param("programId") Integer programId
    );

    // Get semesters available for a particular program and graduating year.
    @Query("""
            SELECT DISTINCT s.semester
            FROM Student s
            WHERE s.program.programId = :programId
              AND s.classOf = :classOf
            ORDER BY s.semester ASC
            """)
    List<Integer> findDistinctSemestersByProgramAndClassOf(
            @Param("programId") Integer programId,
            @Param("classOf") Integer classOf
    );

    @Query("""
            SELECT DISTINCT s.section
            FROM Student s
            WHERE s.program.programId = :programId
              AND s.classOf = :classOf
              AND s.semester = :semester
            ORDER BY s.section ASC
            """)
    List<String> findDistinctSectionsByProgramClassOfAndSemester(
            @Param("programId") Integer programId,
            @Param("classOf") Integer classOf,
            @Param("semester") Integer semester
    );

    // Existing method — retained for compatibility.
    @Query("SELECT DISTINCT s.section FROM Student s ORDER BY s.section ASC")
    List<String> findDistinctSections();
}
