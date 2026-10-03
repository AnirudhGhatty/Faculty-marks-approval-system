package com.SP.marksapproval.service;

import com.SP.marksapproval.entity.FacultySubject;
import com.SP.marksapproval.repository.FacultySubjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FacultySubjectService {

    private final FacultySubjectRepository facultySubjectRepository;

    public FacultySubjectService(FacultySubjectRepository facultySubjectRepository) {
        this.facultySubjectRepository = facultySubjectRepository;
    }

    public List<FacultySubject> getAllAssignments() {
        return facultySubjectRepository.findAll();
    }

    public Optional<FacultySubject> getAssignmentById(Integer id) {
        return facultySubjectRepository.findById(id);
    }

    public FacultySubject saveAssignment(FacultySubject assignment) {
        return facultySubjectRepository.save(assignment);
    }

    public void deleteAssignment(Integer id) {
        facultySubjectRepository.deleteById(id);
    }
}