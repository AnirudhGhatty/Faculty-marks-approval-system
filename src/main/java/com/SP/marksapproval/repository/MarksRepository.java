package com.SP.marksapproval.repository;

import com.SP.marksapproval.entity.Marks;
import com.SP.marksapproval.entity.MarksId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarksRepository extends JpaRepository<Marks, MarksId> {
}