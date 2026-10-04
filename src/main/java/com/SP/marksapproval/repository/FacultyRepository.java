package com.SP.marksapproval.repository;

import com.SP.marksapproval.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FacultyRepository extends JpaRepository<Faculty, Integer> {

    Optional<Faculty> findByEmail(String email);
}