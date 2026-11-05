package com.SP.marksapproval.service;

import com.SP.marksapproval.entity.Student;
import com.SP.marksapproval.entity.Subject;
import com.SP.marksapproval.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public long countByProgramId(Integer programId) {
        return studentRepository.countByProgram_ProgramId(programId);
    }

    public Optional<Student> getStudentById(Integer id) {
        return studentRepository.findById(id);
    }

    public Optional<Student> getStudentByRollNo(String rollNo) {
        return studentRepository.findByRollNo(rollNo);
    }

    public Student saveStudent(Student student) {
        return studentRepository.save(student);
    }

    public void deleteStudent(Integer id) {
        studentRepository.deleteById(id);
    }

    // Get all graduating years.
    public List<Integer> getDistinctClassOf() {
        return studentRepository.findDistinctClassOf();
    }

    // Get graduating years belonging to a particular program.
    public List<Integer> getClassOfByProgram(Integer programId) {
        if (programId == null) {
            return List.of();
        }

        return studentRepository.findDistinctClassOfByProgram(programId);
    }

    // Get semesters for a particular program and graduating year.
    public List<Integer> getSemestersByProgramAndClassOf(
            Integer programId,
            Integer classOf) {

        if (programId == null || classOf == null) {
            return List.of();
        }

        return studentRepository
                .findDistinctSemestersByProgramAndClassOf(programId, classOf);
    }

    public List<String> getDistinctSections() {
        return studentRepository.findDistinctSections();
    }

    public List<String> getDistinctSectionsForSubject(Subject subject) {
        if (subject == null || subject.getProgram() == null
                || subject.getProgram().getProgramId() == null
                || subject.getClassOf() == null || subject.getSemester() == null) {
            return List.of();
        }
        return studentRepository.findDistinctSectionsByProgramClassOfAndSemester(
                subject.getProgram().getProgramId(),
                subject.getClassOf(),
                subject.getSemester());
    }

    // Retrieve students from the selected graduating cohort.
    public List<Student> getStudentsForSubjectSection(
            Subject subject,
            Integer classOf,
            String section) {

        if (subject == null
                || subject.getProgram() == null
                || classOf == null
                || section == null
                || section.isBlank()) {
            return List.of();
        }

        return studentRepository
                .findByProgram_ProgramIdAndClassOfAndSemesterAndSectionIgnoreCaseOrderByRollNoAsc(
                        subject.getProgram().getProgramId(),
                        classOf,
                        subject.getSemester(),
                        section.trim()
                );
    }

    // Retained for compatibility with the existing faculty page.
    public List<Student> getStudentsForSubjectSection(
            Subject subject,
            String section) {

        if (subject == null
                || subject.getProgram() == null
                || section == null
                || section.isBlank()) {
            return List.of();
        }

        return studentRepository
                .findByProgram_ProgramIdAndSemesterAndSectionIgnoreCaseOrderByRollNoAsc(
                        subject.getProgram().getProgramId(),
                        subject.getSemester(),
                        section.trim()
                );
    }
}
