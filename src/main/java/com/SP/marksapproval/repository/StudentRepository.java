package com.SP.marksapproval.repository;

import com.SP.marksapproval.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface StudentRepository extends JpaRepository<Student, Integer> {
    Optional<Student> findByRollNo(String rollNo);
}
