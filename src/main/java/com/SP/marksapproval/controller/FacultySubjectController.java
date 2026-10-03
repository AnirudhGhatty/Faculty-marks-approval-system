package com.SP.marksapproval.controller;

import com.SP.marksapproval.entity.FacultySubject;
import com.SP.marksapproval.service.FacultySubjectService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/faculty-subject")
public class FacultySubjectController {

    private final FacultySubjectService facultySubjectService;

    public FacultySubjectController(FacultySubjectService facultySubjectService) {
        this.facultySubjectService = facultySubjectService;
    }

    @GetMapping
    public List<FacultySubject> getAllAssignments() {
        return facultySubjectService.getAllAssignments();
    }

    @GetMapping("/{id}")
    public Optional<FacultySubject> getAssignmentById(@PathVariable Integer id) {
        return facultySubjectService.getAssignmentById(id);
    }

    @PostMapping
    public FacultySubject saveAssignment(@RequestBody FacultySubject assignment) {
        return facultySubjectService.saveAssignment(assignment);
    }

    @DeleteMapping("/{id}")
    public void deleteAssignment(@PathVariable Integer id) {
        facultySubjectService.deleteAssignment(id);
    }
}