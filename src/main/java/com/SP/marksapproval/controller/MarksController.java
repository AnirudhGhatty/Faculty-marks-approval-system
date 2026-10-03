package com.SP.marksapproval.controller;

import com.SP.marksapproval.entity.Marks;
import com.SP.marksapproval.entity.MarksId;
import com.SP.marksapproval.service.MarksService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/marks")
public class MarksController {

    private final MarksService marksService;

    public MarksController(MarksService marksService) {
        this.marksService = marksService;
    }

    @GetMapping
    public List<Marks> getAllMarks() {
        return marksService.getAllMarks();
    }

    @GetMapping("/{studentId}/{subjectId}")
    public Optional<Marks> getMarksById(
            @PathVariable Integer studentId,
            @PathVariable Integer subjectId) {

        MarksId id = new MarksId(studentId, subjectId);

        return marksService.getMarksById(id);
    }

    @PostMapping
    public Marks saveMarks(@RequestBody Marks marks) {
        return marksService.saveMarks(marks);
    }

    @DeleteMapping("/{studentId}/{subjectId}")
    public void deleteMarks(
            @PathVariable Integer studentId,
            @PathVariable Integer subjectId) {

        MarksId id = new MarksId(studentId, subjectId);

        marksService.deleteMarks(id);
    }
}