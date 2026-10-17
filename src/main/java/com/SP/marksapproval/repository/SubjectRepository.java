package com.SP.marksapproval.repository;

import com.SP.marksapproval.entity.Subject;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, Integer> {
    List<Subject> findByProgram_ProgramIdAndSemester(
            Integer programId,
            Integer semester
    );
}