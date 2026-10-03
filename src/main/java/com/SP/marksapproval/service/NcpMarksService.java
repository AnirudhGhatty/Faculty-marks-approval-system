package com.SP.marksapproval.service;

import com.SP.marksapproval.entity.NcpMarks;
import com.SP.marksapproval.repository.NcpMarksRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NcpMarksService {

    private final NcpMarksRepository ncpMarksRepository;

    public NcpMarksService(NcpMarksRepository ncpMarksRepository) {
        this.ncpMarksRepository = ncpMarksRepository;
    }

    public List<NcpMarks> getAllNcpMarks() {
        return ncpMarksRepository.findAll();
    }

    public Optional<NcpMarks> getNcpMarksById(Integer id) {
        return ncpMarksRepository.findById(id);
    }

    public NcpMarks saveNcpMarks(NcpMarks ncpMarks) {
        return ncpMarksRepository.save(ncpMarks);
    }

    public void deleteNcpMarks(Integer id) {
        ncpMarksRepository.deleteById(id);
    }
}