
package com.SP.marksapproval.service;

import com.SP.marksapproval.entity.Subject;
import com.SP.marksapproval.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public long countByProgramId(Integer programId) {
        return subjectRepository.countByProgram_ProgramId(programId);
    }

    public Optional<Subject> getSubjectById(Integer id) {
        return subjectRepository.findById(id);
    }

    public Subject saveSubject(Subject subject) {
        return subjectRepository.save(subject);
    }

    public void deleteSubject(Integer id) {
        subjectRepository.deleteById(id);
    }

    public List<Subject> getSubjectsByProgramAndSemester(
            Integer programId,
            Integer semester) {

        return subjectRepository
                .findByProgram_ProgramIdAndSemester(programId, semester);
    }

    public List<Subject> getSubjectsByProgramClassOfAndSemester(
            Integer programId,
            Integer classOf,
            Integer semester) {

        return subjectRepository
                .findByProgram_ProgramIdAndClassOfAndSemester(
                        programId, classOf, semester);
    }
}
