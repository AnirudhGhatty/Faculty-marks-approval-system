package com.SP.marksapproval.repository;

import com.SP.marksapproval.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, Integer> {
}