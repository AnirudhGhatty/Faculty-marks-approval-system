package com.SP.marksapproval.repository;

import com.SP.marksapproval.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacultyRepository extends JpaRepository<Faculty, Integer> {
}