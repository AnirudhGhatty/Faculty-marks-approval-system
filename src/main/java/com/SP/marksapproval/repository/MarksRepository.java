package com.SP.marksapproval.repository;

import com.SP.marksapproval.entity.Marks;
import com.SP.marksapproval.entity.MarksId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MarksRepository extends JpaRepository<Marks, MarksId> {
    List<Marks> findByStudent_StudentId(Integer studentId);

}