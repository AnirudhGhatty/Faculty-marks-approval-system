
package com.SP.marksapproval.repository;

import com.SP.marksapproval.entity.Program;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProgramRepository extends JpaRepository<Program, Integer> {

    Optional<Program> findByProgramNameIgnoreCase(String programName);
}
