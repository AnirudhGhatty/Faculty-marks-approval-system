package com.SP.marksapproval.service;

import com.SP.marksapproval.entity.Marks;
import com.SP.marksapproval.entity.MarksId;
import com.SP.marksapproval.repository.MarksRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MarksService {

    private final MarksRepository marksRepository;

    public MarksService(MarksRepository marksRepository) {
        this.marksRepository = marksRepository;
    }

    public List<Marks> getAllMarks() {
        return marksRepository.findAll();
    }

    public Optional<Marks> getMarksById(MarksId id) {
        return marksRepository.findById(id);
    }

    public Marks saveMarks(Marks marks) {
        return marksRepository.save(marks);
    }

    public void deleteMarks(MarksId id) {
        marksRepository.deleteById(id);
    }
}