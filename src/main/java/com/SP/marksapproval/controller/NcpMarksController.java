package com.SP.marksapproval.controller;

import com.SP.marksapproval.entity.NcpMarks;
import com.SP.marksapproval.service.NcpMarksService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/ncp-marks")
public class NcpMarksController {

    private final NcpMarksService ncpMarksService;

    public NcpMarksController(NcpMarksService ncpMarksService) {
        this.ncpMarksService = ncpMarksService;
    }

    @GetMapping
    public List<NcpMarks> getAllNcpMarks() {
        return ncpMarksService.getAllNcpMarks();
    }

    @GetMapping("/{id}")
    public Optional<NcpMarks> getNcpMarksById(@PathVariable Integer id) {
        return ncpMarksService.getNcpMarksById(id);
    }

    @PostMapping
    public NcpMarks saveNcpMarks(@RequestBody NcpMarks ncpMarks) {
        return ncpMarksService.saveNcpMarks(ncpMarks);
    }

    @DeleteMapping("/{id}")
    public void deleteNcpMarks(@PathVariable Integer id) {
        ncpMarksService.deleteNcpMarks(id);
    }
}