package com.SP.marksapproval.controller;

import com.SP.marksapproval.entity.Faculty;
import com.SP.marksapproval.service.FacultyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/faculty")
public class FacultyController {

    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping
    public List<Faculty> getAllFaculty() {
        return facultyService.getAllFaculty();
    }

    @GetMapping("/{id}")
    public Optional<Faculty> getFacultyById(@PathVariable Integer id) {
        return facultyService.getFacultyById(id);
    }

    @PostMapping
    public Faculty saveFaculty(@RequestBody Faculty faculty) {
        return facultyService.saveFaculty(faculty);
    }

    @DeleteMapping("/{id}")
    public void deleteFaculty(@PathVariable Integer id) {
        facultyService.deleteFaculty(id);
    }
}