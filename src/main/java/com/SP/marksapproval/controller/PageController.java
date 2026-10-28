
package com.SP.marksapproval.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.SP.marksapproval.entity.NcpMarks;
import com.SP.marksapproval.service.NcpMarksService;

import org.springframework.security.core.Authentication;

import com.SP.marksapproval.entity.Faculty;
import com.SP.marksapproval.entity.FacultySubject;
import com.SP.marksapproval.entity.Program;
import com.SP.marksapproval.entity.Student;
import com.SP.marksapproval.entity.Subject;
import com.SP.marksapproval.repository.ProgramRepository;
import com.SP.marksapproval.service.FacultyService;
import com.SP.marksapproval.service.FacultySubjectService;
import com.SP.marksapproval.service.StudentService;
import com.SP.marksapproval.service.SubjectService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

    private final StudentService studentService;
    private final ProgramRepository programRepository;
    private final FacultyService facultyService;
    private final SubjectService subjectService;
    private final FacultySubjectService facultySubjectService;
    private final NcpMarksService ncpMarksService;

    public PageController(
            StudentService studentService,
            ProgramRepository programRepository,
            FacultyService facultyService,
            SubjectService subjectService,
            FacultySubjectService facultySubjectService,
            NcpMarksService ncpMarksService) {

        this.studentService = studentService;
        this.programRepository = programRepository;
        this.facultyService = facultyService;
        this.subjectService = subjectService;
        this.facultySubjectService = facultySubjectService;
        this.ncpMarksService = ncpMarksService;
    }

    // =========================
    // LOGIN AND DASHBOARDS
    // =========================

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/student-dashboard")
    public String studentDashboard() {
        return "student-dashboard";
    }

    @GetMapping("/student-results")
    public String studentResults() {
        return "student-results";
    }

    @GetMapping("/admin-dashboard")
    public String adminDashboard() {
        return "admin-dashboard";
    }

    @GetMapping("/faculty-dashboard")
    public String facultyDashboard(
            Authentication authentication,
            Model model) {

        if (authentication == null) {
            return "redirect:/login";
        }

        String[] loginParts = authentication.getName().split(":", 2);

        if (loginParts.length != 2
                || !"faculty".equalsIgnoreCase(loginParts[0])) {
            return "redirect:/faculty-dashboard";
        }

        Faculty faculty = facultyService
                .getFacultyByEmail(loginParts[1])
                .orElse(null);

        if (faculty == null) {
            return "redirect:/login";
        }

        List<FacultySubject> assignments =
                facultySubjectService.getAssignmentsByFaculty(
                        faculty.getFacultyId());

        model.addAttribute("faculty", faculty);
        model.addAttribute("assignments", assignments);

        return "faculty-dashboard";
    }

    @GetMapping("/marks-entry")
    public String marksEntry(
            Authentication authentication,
            @RequestParam(required = false) Integer assignmentId,
            @RequestParam(required = false) String ncp,
            Model model) {

        if (authentication == null) {
            return "redirect:/login";
        }

        String[] loginParts = authentication.getName().split(":", 2);

        if (loginParts.length != 2
                || !"faculty".equalsIgnoreCase(loginParts[0])) {
            return "redirect:/faculty-dashboard";
        }

        Faculty faculty = facultyService
                .getFacultyByEmail(loginParts[1])
                .orElse(null);

        if (faculty == null) {
            return "redirect:/faculty-dashboard?error=account";
        }

        List<FacultySubject> assignments =
                facultySubjectService.getAssignmentsByFaculty(
                        faculty.getFacultyId());

        model.addAttribute("faculty", faculty);
        model.addAttribute("assignments", assignments);

        FacultySubject selectedAssignment = null;

        if (assignmentId != null) {
            selectedAssignment = assignments.stream()
                    .filter(assignment ->
                            assignment.getAssignmentId().equals(assignmentId))
                    .findFirst()
                    .orElse(null);

            if (selectedAssignment == null) {
                model.addAttribute(
                        "errorMessage",
                        "That subject and section are not assigned to your account.");
            } else {
                List<Student> selectedStudents =
                        studentService.getStudentsForSubjectSection(
                                selectedAssignment.getSubject(),
                                selectedAssignment.getSection());

                model.addAttribute("students", selectedStudents);

                Map<Integer, NcpMarks> ncpMarksByStudent =
                        ncpMarksService.getNcpMarksBySubjectId(
                                        selectedAssignment.getSubject()
                                                .getSubjectId())
                                .stream()
                                .filter(record -> selectedStudents.stream()
                                        .anyMatch(student ->
                                                student.getStudentId().equals(
                                                        record.getStudent()
                                                                .getStudentId())))
                                .collect(Collectors.toMap(
                                        record -> record.getStudent()
                                                .getStudentId(),
                                        record -> record));

                model.addAttribute(
                        "ncpMarksByStudent",
                        ncpMarksByStudent);
            }
        }

        if (ncp != null
                && !List.of("NCP1", "NCP2", "NCP3").contains(ncp)) {

            model.addAttribute(
                    "errorMessage",
                    "Invalid NCP selection.");

            ncp = null;
        }

        model.addAttribute("selectedAssignment", selectedAssignment);
        model.addAttribute("selectedNcp", ncp);

        return "marks-entry";
    }

    // =========================
    // STUDENT MANAGEMENT
    // =========================

    @GetMapping("/admin/students")
    public String adminStudents(Model model) {
        model.addAttribute("students", studentService.getAllStudents());
        model.addAttribute("programs", programRepository.findAll());

        return "admin-students";
    }

    @PostMapping("/admin/students")
    public String addStudent(
            @RequestParam String rollNo,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam Integer programId,
            @RequestParam Integer semester,
            @RequestParam String section) {

        Program program = programRepository.findById(programId)
                .orElse(null);

        if (program == null) {
            return "redirect:/admin/students?error=program";
        }

        Student student = new Student();
        student.setRollNo(rollNo);
        student.setName(name);
        student.setEmail(email);
        student.setPassword(password);
        student.setProgram(program);
        student.setSemester(semester);
        student.setSection(section);

        studentService.saveStudent(student);

        return "redirect:/admin/students?success";
    }

    @PostMapping("/admin/students/delete/{rollNo}")
    public String deleteStudent(@PathVariable String rollNo) {

        Student student = studentService.getStudentByRollNo(rollNo)
                .orElse(null);

        if (student == null) {
            return "redirect:/admin/students?error=notfound";
        }

        studentService.deleteStudent(student.getStudentId());

        return "redirect:/admin/students?deleted";
    }

    @PostMapping("/admin/students/update/{rollNo}")
    public String updateStudent(
            @PathVariable String rollNo,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam(required = false) String password,
            @RequestParam Integer programId,
            @RequestParam Integer semester,
            @RequestParam String section) {

        Student student = studentService.getStudentByRollNo(rollNo)
                .orElse(null);

        if (student == null) {
            return "redirect:/admin/students?error=notfound";
        }

        Program program = programRepository.findById(programId)
                .orElse(null);

        if (program == null) {
            return "redirect:/admin/students?error=program";
        }

        student.setName(name);
        student.setEmail(email);
        student.setProgram(program);
        student.setSemester(semester);
        student.setSection(section);

        if (password != null && !password.isBlank()) {
            student.setPassword(password);
        }

        studentService.saveStudent(student);

        return "redirect:/admin/students?updated";
    }

    // =========================
    // FACULTY MANAGEMENT
    // =========================

    @GetMapping("/admin/faculty")
    public String adminFaculty(Model model) {

        List<Faculty> facultyMembers = facultyService.getAllFaculty();

        Map<Integer, List<FacultySubject>> assignmentsByFaculty =
                facultySubjectService.getAllAssignments()
                        .stream()
                        .collect(Collectors.groupingBy(
                                assignment ->
                                        assignment.getFaculty().getFacultyId()
                        ));

        model.addAttribute("facultyMembers", facultyMembers);
        model.addAttribute("subjects", subjectService.getAllSubjects());
        model.addAttribute("assignmentsByFaculty", assignmentsByFaculty);

        return "admin-faculty";
    }

    @PostMapping("/admin/faculty")
    public String addFaculty(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String department,
            @RequestParam String password) {

        if (facultyService.getFacultyByEmail(email).isPresent()) {
            return "redirect:/admin/faculty?error=email";
        }

        Faculty faculty = new Faculty();
        faculty.setName(name);
        faculty.setEmail(email);
        faculty.setDepartment(department);
        faculty.setPassword(password);

        facultyService.saveFaculty(faculty);

        return "redirect:/admin/faculty?success";
    }

    @PostMapping("/admin/faculty/delete/{id}")
    public String deleteFaculty(@PathVariable Integer id) {

        Faculty faculty = facultyService.getFacultyById(id)
                .orElse(null);

        if (faculty == null) {
            return "redirect:/admin/faculty?error=notfound";
        }

        facultyService.deleteFaculty(id);

        return "redirect:/admin/faculty?deleted";
    }

    @PostMapping("/admin/faculty/update/{id}")
    public String updateFaculty(
            @PathVariable Integer id,
            @RequestParam String name,
            @RequestParam String department) {

        Faculty faculty = facultyService.getFacultyById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Faculty not found with ID: " + id));

        faculty.setName(name);
        faculty.setDepartment(department);

        facultyService.saveFaculty(faculty);

        return "redirect:/admin/faculty?updated";
    }

    // =========================
    // SUBJECT MANAGEMENT
    // =========================

    @GetMapping("/admin/subjects")
    public String adminSubjects(Model model) {
        model.addAttribute("subjects", subjectService.getAllSubjects());
        model.addAttribute("programs", programRepository.findAll());

        return "admin-subjects";
    }

    @PostMapping("/admin/subjects")
    public String addSubject(
            @RequestParam String subjectName,
            @RequestParam Integer credits,
            @RequestParam Integer semester,
            @RequestParam String courseCode,
            @RequestParam Integer programId) {

        Program program = programRepository.findById(programId)
                .orElse(null);

        if (program == null) {
            return "redirect:/admin/subjects?error=program";
        }

        Subject subject = new Subject();
        subject.setSubjectName(subjectName);
        subject.setCredits(credits);
        subject.setSemester(semester);
        subject.setCourseCode(courseCode);
        subject.setProgram(program);

        subjectService.saveSubject(subject);

        return "redirect:/admin/subjects?success";
    }

    @PostMapping("/admin/subjects/update/{id}")
    public String updateSubject(
            @PathVariable Integer id,
            @RequestParam String subjectName,
            @RequestParam Integer credits,
            @RequestParam Integer semester,
            @RequestParam String courseCode,
            @RequestParam Integer programId) {

        Subject subject = subjectService.getSubjectById(id)
                .orElse(null);

        if (subject == null) {
            return "redirect:/admin/subjects?error=notfound";
        }

        Program program = programRepository.findById(programId)
                .orElse(null);

        if (program == null) {
            return "redirect:/admin/subjects?error=program";
        }

        subject.setSubjectName(subjectName);
        subject.setCredits(credits);
        subject.setSemester(semester);
        subject.setCourseCode(courseCode);
        subject.setProgram(program);

        subjectService.saveSubject(subject);

        return "redirect:/admin/subjects?updated";
    }

    @PostMapping("/admin/subjects/delete/{id}")
    public String deleteSubject(@PathVariable Integer id) {

        Subject subject = subjectService.getSubjectById(id)
                .orElse(null);

        if (subject == null) {
            return "redirect:/admin/subjects?error=notfound";
        }

        subjectService.deleteSubject(id);

        return "redirect:/admin/subjects?deleted";
    }

    // =========================
    // FACULTY-SUBJECT ASSIGNMENT
    // =========================

    @GetMapping("/admin/faculty-subjects")
    public String adminFacultySubjects(Model model) {

        model.addAttribute(
                "assignments",
                facultySubjectService.getAllAssignments()
        );

        model.addAttribute(
                "sectionOptions",
                studentService.getDistinctSections()
        );

        model.addAttribute(
                "facultyMembers",
                facultyService.getAllFaculty()
        );

        model.addAttribute(
                "subjects",
                subjectService.getAllSubjects()
        );

        return "admin-faculty-subjects";
    }

    @PostMapping("/admin/faculty-subjects")
    public String addFacultySubject(
            @RequestParam Integer facultyId,
            @RequestParam Integer subjectId,
            @RequestParam String section) {

        Faculty faculty = facultyService.getFacultyById(facultyId)
                .orElse(null);

        if (faculty == null) {
            return "redirect:/admin/faculty-subjects?error=faculty";
        }

        Subject subject = subjectService.getSubjectById(subjectId)
                .orElse(null);

        if (subject == null) {
            return "redirect:/admin/faculty-subjects?error=subject";
        }

        String normalizedSection =
                section == null ? "" : section.trim();

        if (normalizedSection.isBlank()
                || normalizedSection.length() > 20) {

            return "redirect:/admin/faculty-subjects?error=section";
        }

        if (studentService
                .getStudentsForSubjectSection(subject, normalizedSection)
                .isEmpty()) {

            return "redirect:/admin/faculty-subjects?error=sectionstudents";
        }

        // Prevent duplicate assignments for the same faculty,
        // subject, and section. Other faculty can share this
        // subject-section assignment.
        if (facultySubjectService.assignmentExists(
                facultyId,
                subjectId,
                normalizedSection)) {

            return "redirect:/admin/faculty-subjects?error=duplicate";
        }

        FacultySubject assignment = new FacultySubject();
        assignment.setFaculty(faculty);
        assignment.setSubject(subject);
        assignment.setSection(normalizedSection);

        facultySubjectService.saveAssignment(assignment);

        return "redirect:/admin/faculty-subjects?success";
    }

    @PostMapping("/admin/faculty-subjects/delete/{id}")
    public String deleteFacultySubject(@PathVariable Integer id) {

        if (facultySubjectService.getAssignmentById(id).isEmpty()) {
            return "redirect:/admin/faculty-subjects?error=notfound";
        }

        facultySubjectService.deleteAssignment(id);

        return "redirect:/admin/faculty-subjects?deleted";
    }
}
