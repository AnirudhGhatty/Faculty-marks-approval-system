package com.SP.marksapproval.repository;

import com.SP.marksapproval.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Integer> {
}
