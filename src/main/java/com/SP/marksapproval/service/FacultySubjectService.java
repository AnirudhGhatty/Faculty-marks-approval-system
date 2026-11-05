
package com.SP.marksapproval.service;

import com.SP.marksapproval.entity.FacultySubject;
import com.SP.marksapproval.repository.FacultySubjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FacultySubjectService {

    private final FacultySubjectRepository facultySubjectRepository;

    public FacultySubjectService(
            FacultySubjectRepository facultySubjectRepository) {
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

    public List<FacultySubject> getAssignmentsByFaculty(Integer facultyId) {
        return facultySubjectRepository
                .findByFaculty_FacultyId(facultyId);
    }

    public List<FacultySubject> getAssignmentsBySubject(Integer subjectId) {
        return facultySubjectRepository
                .findBySubject_SubjectId(subjectId);
    }

    public List<FacultySubject> getAssignmentsByFacultyAndSubject(
            Integer facultyId,
            Integer subjectId) {

        return facultySubjectRepository
                .findByFaculty_FacultyIdAndSubject_SubjectId(
                        facultyId, subjectId);
    }

    public List<FacultySubject> getAssignmentsByFacultyAndProgram(
            Integer facultyId,
            Integer programId) {

        return facultySubjectRepository
                .findByFaculty_FacultyIdAndSubject_Program_ProgramId(
                        facultyId, programId);
    }

    public List<FacultySubject> getAssignmentsByFacultyProgramAndSemester(
            Integer facultyId,
            Integer programId,
            Integer semester) {

        return facultySubjectRepository
                .findByFaculty_FacultyIdAndSubject_Program_ProgramIdAndSubject_Semester(
                        facultyId, programId, semester);
    }

    public List<FacultySubject> getAssignmentsByFacultyProgramClassOfAndSemester(
            Integer facultyId,
            Integer programId,
            Integer classOf,
            Integer semester) {

        return facultySubjectRepository
                .findByFaculty_FacultyIdAndSubject_Program_ProgramIdAndSubject_ClassOfAndSubject_Semester(
                        facultyId, programId, classOf, semester);
    }

    public boolean assignmentExists(
            Integer facultyId,
            Integer subjectId,
            String section) {

        return facultySubjectRepository
                .existsByFaculty_FacultyIdAndSubject_SubjectIdAndSectionIgnoreCase(
                        facultyId, subjectId, section);
    }
}
