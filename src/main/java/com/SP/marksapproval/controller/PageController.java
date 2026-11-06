
package com.SP.marksapproval.controller;

import java.math.BigDecimal;
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
    public String studentDashboard(
            Authentication authentication,
            Model model) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String[] loginParts = authentication.getName().split(":", 2);

        if (loginParts.length != 2
                || !"student".equalsIgnoreCase(loginParts[0])) {
            return "redirect:/login";
        }

        Student student = studentService
                .getStudentByRollNo(loginParts[1])
                .orElse(null);

        if (student == null) {
            return "redirect:/login";
        }

        List<Subject> subjects =
                subjectService.getSubjectsByProgramClassOfAndSemester(
                        student.getProgram().getProgramId(),
                        student.getClassOf(),
                        student.getSemester());

        List<NcpMarks> ncpRecords =
                ncpMarksService.getNcpMarksByStudentId(
                        student.getStudentId());

        Map<Integer, BigDecimal> externalMarksBySubject =
                new java.util.HashMap<>();

        for (NcpMarks record : ncpRecords) {

            BigDecimal total = BigDecimal.ZERO;
            boolean hasSubmittedMarks = false;

            if (Boolean.TRUE.equals(record.getNcp1Locked())
                    && record.getNcp1() != null) {
                total = total.add(record.getNcp1());
                hasSubmittedMarks = true;
            }

            if (Boolean.TRUE.equals(record.getNcp2Locked())
                    && record.getNcp2() != null) {
                total = total.add(record.getNcp2());
                hasSubmittedMarks = true;
            }

            if (Boolean.TRUE.equals(record.getNcp3Locked())
                    && record.getNcp3() != null) {
                total = total.add(record.getNcp3());
                hasSubmittedMarks = true;
            }

            if (hasSubmittedMarks) {
                externalMarksBySubject.put(
                        record.getSubject().getSubjectId(),
                        total);
            }
        }

        model.addAttribute("student", student);
        model.addAttribute("subjects", subjects);
        model.addAttribute("externalMarksBySubject", externalMarksBySubject);

        return "student-dashboard";
    }

    @GetMapping("/student-results")
    public String studentResults(
            Authentication authentication,
            @RequestParam(required = false) String ncp,
            Model model) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String[] loginParts = authentication.getName().split(":", 2);

        if (loginParts.length != 2
                || !"student".equalsIgnoreCase(loginParts[0])) {
            return "redirect:/login";
        }

        Student student = studentService
                .getStudentByRollNo(loginParts[1])
                .orElse(null);

        if (student == null) {
            return "redirect:/login";
        }

        List<String> validNcpOptions = List.of("NCP1", "NCP2", "NCP3");

        if (ncp == null || !validNcpOptions.contains(ncp.toUpperCase())) {
            ncp = "NCP1";
        } else {
            ncp = ncp.toUpperCase();
        }

        List<Subject> subjects =
                subjectService.getSubjectsByProgramClassOfAndSemester(
                        student.getProgram().getProgramId(),
                        student.getClassOf(),
                        student.getSemester());

        List<NcpMarks> ncpRecords =
                ncpMarksService.getNcpMarksByStudentId(
                        student.getStudentId());

        Map<Integer, NcpMarks> ncpMarksBySubject =
                new java.util.HashMap<>();

        for (NcpMarks record : ncpRecords) {
            ncpMarksBySubject.put(
                    record.getSubject().getSubjectId(),
                    record);
        }

        model.addAttribute("student", student);
        model.addAttribute("subjects", subjects);
        model.addAttribute("ncpMarksBySubject", ncpMarksBySubject);
        model.addAttribute("selectedNcp", ncp);

        return "student-results";
    }

    @GetMapping("/admin-dashboard")
    public String adminDashboard(Model model) {

        model.addAttribute("totalStudents",
                studentService.getAllStudents().size());

        model.addAttribute("totalFaculty",
                facultyService.getAllFaculty().size());

        model.addAttribute("totalSubjects",
                subjectService.getAllSubjects().size());

        List<FacultySubject> assignments =
                facultySubjectService.getAllAssignments();

        List<Map<String, Object>> dashboardRows =
                new java.util.ArrayList<>();

        for (FacultySubject assignment : assignments) {

            Subject subject = assignment.getSubject();

            List<Student> students =
                    studentService.getStudentsForSubjectSection(
                            subject,
                            subject.getClassOf(),
                            assignment.getSection());

            List<NcpMarks> records =
                    ncpMarksService.getNcpMarksBySubjectId(
                            subject.getSubjectId());

            java.util.Set<Integer> studentIds = students.stream()
                    .map(Student::getStudentId)
                    .collect(Collectors.toSet());

            List<NcpMarks> relevantRecords = records.stream()
                    .filter(record -> studentIds.contains(
                            record.getStudent().getStudentId()))
                    .collect(Collectors.toList());

            Map<String, Object> row = new java.util.HashMap<>();

            row.put("assignmentId", assignment.getAssignmentId());
            row.put("facultyName", assignment.getFaculty().getName());
            row.put("subjectName", subject.getSubjectName());
            row.put("classOf", subject.getClassOf());
            row.put("section", assignment.getSection());
            row.put("studentCount", students.size());

            row.put("ncp1Status",
                    getNcpSubmissionStatus(
                            relevantRecords, students.size(), "NCP1"));

            row.put("ncp2Status",
                    getNcpSubmissionStatus(
                            relevantRecords, students.size(), "NCP2"));

            row.put("ncp3Status",
                    getNcpSubmissionStatus(
                            relevantRecords, students.size(), "NCP3"));

            dashboardRows.add(row);
        }

        model.addAttribute("dashboardRows", dashboardRows);

        return "admin-dashboard";
    }

    private String getNcpSubmissionStatus(
            List<NcpMarks> records,
            int totalStudents,
            String ncp) {

        if (totalStudents == 0) {
            return "Not Submitted";
        }

        int submittedCount = 0;

        for (NcpMarks record : records) {

            boolean locked = false;
            BigDecimal score = null;

            switch (ncp) {

                case "NCP1":
                    locked = Boolean.TRUE.equals(record.getNcp1Locked());
                    score = record.getNcp1();
                    break;

                case "NCP2":
                    locked = Boolean.TRUE.equals(record.getNcp2Locked());
                    score = record.getNcp2();
                    break;

                case "NCP3":
                    locked = Boolean.TRUE.equals(record.getNcp3Locked());
                    score = record.getNcp3();
                    break;

                default:
                    throw new IllegalArgumentException("Invalid NCP.");
            }

            if (locked && score != null) {
                submittedCount++;
            }
        }

        if (submittedCount == 0) {
            return "Not Submitted";
        }

        if (submittedCount < totalStudents) {
            return "Partially Submitted";
        }

        return "Submitted";
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

        java.util.Set<Integer> uniqueStudentIds =
                new java.util.HashSet<>();

        int ncp1Pending = 0;
        int ncp2Pending = 0;
        int ncp3Pending = 0;

        for (FacultySubject assignment : assignments) {

            List<Student> students =
                    studentService.getStudentsForSubjectSection(
                            assignment.getSubject(),
                            assignment.getSubject().getClassOf(),
                            assignment.getSection());

            for (Student student : students) {
                uniqueStudentIds.add(student.getStudentId());
            }

            List<NcpMarks> records =
                    ncpMarksService.getNcpMarksBySubjectId(
                            assignment.getSubject().getSubjectId());

            java.util.Set<Integer> assignedStudentIds = students.stream()
                    .map(Student::getStudentId)
                    .collect(Collectors.toSet());

            Map<Integer, NcpMarks> recordsByStudent = records.stream()
                    .filter(record -> assignedStudentIds.contains(
                            record.getStudent().getStudentId()))
                    .collect(Collectors.toMap(
                            record -> record.getStudent().getStudentId(),
                            record -> record,
                            (first, second) -> first
                    ));

            for (Student student : students) {

                NcpMarks record =
                        recordsByStudent.get(student.getStudentId());

                if (record == null
                        || !Boolean.TRUE.equals(record.getNcp1Locked())) {
                    ncp1Pending++;
                }

                if (record == null
                        || !Boolean.TRUE.equals(record.getNcp2Locked())) {
                    ncp2Pending++;
                }

                if (record == null
                        || !Boolean.TRUE.equals(record.getNcp3Locked())) {
                    ncp3Pending++;
                }
            }
        }

        model.addAttribute("faculty", faculty);
        model.addAttribute("assignments", assignments);
        model.addAttribute("totalStudents", uniqueStudentIds.size());

        model.addAttribute("ncp1Pending", ncp1Pending);
        model.addAttribute("ncp2Pending", ncp2Pending);
        model.addAttribute("ncp3Pending", ncp3Pending);

        return "faculty-dashboard";
    }

    @GetMapping("/marks-entry")
    public String marksEntry(
            Authentication authentication,
            @RequestParam(required = false) Integer assignmentId,
            @RequestParam(required = false) Integer programId,
            @RequestParam(required = false) Integer classOf,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) String ncp,
            Model model) {

        if (authentication == null || !authentication.isAuthenticated()) {
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

        List<Program> programs = assignments.stream()
                .map(assignment -> assignment.getSubject().getProgram())
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toMap(
                        Program::getProgramId,
                        program -> program,
                        (first, second) -> first
                ))
                .values()
                .stream()
                .sorted(java.util.Comparator.comparing(
                        Program::getProgramName))
                .collect(Collectors.toList());

        model.addAttribute("programs", programs);

        List<Integer> classOfOptions = new java.util.ArrayList<>();

        if (programId != null) {
            classOfOptions = assignments.stream()
                    .filter(assignment ->
                            assignment.getSubject().getProgram() != null)
                    .filter(assignment ->
                            assignment.getSubject().getProgram()
                                    .getProgramId().equals(programId))
                    .map(assignment -> assignment.getSubject().getClassOf())
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .sorted(java.util.Comparator.reverseOrder())
                    .collect(Collectors.toList());
        }

        model.addAttribute("classOfOptions", classOfOptions);

        List<FacultySubject> cohortAssignments = assignments.stream()
                .filter(assignment ->
                        programId == null
                                || (assignment.getSubject().getProgram() != null
                                && assignment.getSubject().getProgram()
                                        .getProgramId().equals(programId)))
                .filter(assignment ->
                        classOf == null
                                || assignment.getSubject().getClassOf()
                                        .equals(classOf))
                .collect(Collectors.toList());

        List<Integer> semesters = cohortAssignments.stream()
                .map(assignment -> assignment.getSubject().getSemester())
                .filter(java.util.Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        model.addAttribute("semesters", semesters);

        List<FacultySubject> filteredAssignments =
                cohortAssignments.stream()
                        .filter(assignment ->
                                semester == null
                                        || assignment.getSubject().getSemester()
                                                .equals(semester))
                        .collect(Collectors.toList());

        List<Subject> availableSubjects = filteredAssignments.stream()
                .map(FacultySubject::getSubject)
                .collect(Collectors.toMap(
                        Subject::getSubjectId,
                        subject -> subject,
                        (first, second) -> first
                ))
                .values()
                .stream()
                .sorted(java.util.Comparator.comparing(
                        Subject::getSubjectName))
                .collect(Collectors.toList());

        model.addAttribute("availableSubjects", availableSubjects);

        List<String> sections = new java.util.ArrayList<>();

        if (subjectId != null) {
            sections = filteredAssignments.stream()
                    .filter(assignment ->
                            assignment.getSubject().getSubjectId()
                                    .equals(subjectId))
                    .map(FacultySubject::getSection)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
        }

        model.addAttribute("sections", sections);

        FacultySubject selectedAssignment = null;

        if (assignmentId != null) {
            selectedAssignment = filteredAssignments.stream()
                    .filter(assignment ->
                            assignment.getAssignmentId().equals(assignmentId))
                    .findFirst()
                    .orElse(null);

        } else if (subjectId != null
                && section != null
                && !section.isBlank()) {

            selectedAssignment = filteredAssignments.stream()
                    .filter(assignment ->
                            assignment.getSubject().getSubjectId()
                                    .equals(subjectId))
                    .filter(assignment ->
                            assignment.getSection().equalsIgnoreCase(section))
                    .findFirst()
                    .orElse(null);
        }

        if ((assignmentId != null && selectedAssignment == null)
                || (subjectId != null
                && section != null
                && !section.isBlank()
                && selectedAssignment == null)) {

            model.addAttribute(
                    "errorMessage",
                    "That subject, cohort and section are not assigned to your account.");
        }

        if (selectedAssignment != null) {

            Integer selectedClassOf =
                    selectedAssignment.getSubject().getClassOf();

            List<Student> selectedStudents =
                    studentService.getStudentsForSubjectSection(
                            selectedAssignment.getSubject(),
                            selectedClassOf,
                            selectedAssignment.getSection());

            model.addAttribute("students", selectedStudents);

            java.util.Set<Integer> selectedStudentIds =
                    selectedStudents.stream()
                            .map(Student::getStudentId)
                            .collect(Collectors.toSet());

            Map<Integer, NcpMarks> ncpMarksByStudent =
                    ncpMarksService.getNcpMarksBySubjectId(
                                    selectedAssignment.getSubject().getSubjectId())
                            .stream()
                            .filter(record -> selectedStudentIds.contains(
                                    record.getStudent().getStudentId()))
                            .collect(Collectors.toMap(
                                    record -> record.getStudent().getStudentId(),
                                    record -> record,
                                    (first, second) -> first
                            ));

            model.addAttribute("ncpMarksByStudent", ncpMarksByStudent);
        }

        Integer effectiveClassOf = selectedAssignment != null
                ? selectedAssignment.getSubject().getClassOf()
                : classOf;

        if (ncp != null && ncp.isBlank()) {
            ncp = null;
        }

        if (ncp != null) {

            ncp = ncp.trim().toUpperCase();

            if (!List.of("NCP1", "NCP2", "NCP3").contains(ncp)) {

                model.addAttribute(
                        "errorMessage",
                        "Invalid NCP selection."
                );

                ncp = null;
            }
        }

        model.addAttribute("selectedAssignment", selectedAssignment);
        model.addAttribute("selectedProgramId", programId);
        model.addAttribute("selectedClassOf", effectiveClassOf);
        model.addAttribute("selectedSemester", semester);
        model.addAttribute("selectedSubjectId", subjectId);
        model.addAttribute("selectedSection", section);
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
            @RequestParam Integer classOf,
            @RequestParam String section) {

        if (rollNo.isBlank() || rollNo.length() > 20
                || name.isBlank() || name.length() > 100
                || email.isBlank() || email.length() > 100
                || password.isBlank() || password.length() > 25
                || classOf < 2000 || classOf > 2100
                || semester < 1 || semester > 8
                || section.isBlank() || section.trim().length() > 10) {

            return "redirect:/admin/students?error=fields";
        }

        Program program = programRepository.findById(programId)
                .orElse(null);

        if (program == null) {
            return "redirect:/admin/students?error=program";
        }

        Student student = new Student();

        student.setRollNo(rollNo.trim());
        student.setName(name.trim());
        student.setEmail(email.trim());
        student.setPassword(password);
        student.setProgram(program);
        student.setClassOf(classOf);
        student.setSemester(semester);
        student.setSection(section.trim());

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
            @RequestParam Integer classOf,
            @RequestParam Integer semester,
            @RequestParam String section) {

        if (name.isBlank() || name.length() > 100
                || email.isBlank() || email.length() > 100
                || (password != null && password.length() > 25)
                || classOf < 2000 || classOf > 2100
                || semester < 1 || semester > 8
                || section.isBlank() || section.trim().length() > 10) {

            return "redirect:/admin/students?error=fields";
        }

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

        student.setName(name.trim());
        student.setEmail(email.trim());
        student.setProgram(program);
        student.setClassOf(classOf);
        student.setSemester(semester);
        student.setSection(section.trim());

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

    @GetMapping("/admin/programs")
    public String adminPrograms(Model model) {

        model.addAttribute("programs", programRepository.findAll());

        return "admin-programs";
    }

    @PostMapping("/admin/programs")
    public String addProgram(@RequestParam String programName) {

        String normalizedName =
                programName == null ? "" : programName.trim();

        if (normalizedName.isBlank() || normalizedName.length() > 100) {
            return "redirect:/admin/programs?error=name";
        }

        if (programRepository.findByProgramNameIgnoreCase(normalizedName)
                .isPresent()) {

            return "redirect:/admin/programs?error=duplicate";
        }

        Program program = new Program();
        program.setProgramName(normalizedName);

        programRepository.save(program);

        return "redirect:/admin/programs?success";
    }

    @PostMapping("/admin/programs/update/{id}")
    public String updateProgram(
            @PathVariable Integer id,
            @RequestParam String programName) {

        Program program = programRepository.findById(id).orElse(null);

        if (program == null) {
            return "redirect:/admin/programs?error=notfound";
        }

        String normalizedName =
                programName == null ? "" : programName.trim();

        if (normalizedName.isBlank() || normalizedName.length() > 100) {
            return "redirect:/admin/programs?error=name";
        }

        Program duplicate = programRepository
                .findByProgramNameIgnoreCase(normalizedName)
                .orElse(null);

        if (duplicate != null && !duplicate.getProgramId().equals(id)) {
            return "redirect:/admin/programs?error=duplicate";
        }

        program.setProgramName(normalizedName);
        programRepository.save(program);

        return "redirect:/admin/programs?updated";
    }

    @PostMapping("/admin/programs/delete/{id}")
    public String deleteProgram(@PathVariable Integer id) {

        if (!programRepository.existsById(id)) {
            return "redirect:/admin/programs?error=notfound";
        }

        if (studentService.countByProgramId(id) > 0
                || subjectService.countByProgramId(id) > 0) {

            return "redirect:/admin/programs?error=inuse";
        }

        programRepository.deleteById(id);

        return "redirect:/admin/programs?deleted";
    }

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
            @RequestParam Integer classOf,
            @RequestParam Integer programId) {

        if (subjectName.isBlank() || subjectName.length() > 100
                || courseCode.isBlank() || courseCode.length() > 50
                || credits < 1 || semester < 1 || semester > 8
                || classOf < 2000 || classOf > 2100) {

            return "redirect:/admin/subjects?error=fields";
        }

        Program program = programRepository.findById(programId)
                .orElse(null);

        if (program == null) {
            return "redirect:/admin/subjects?error=program";
        }

        Subject subject = new Subject();

        subject.setSubjectName(subjectName.trim());
        subject.setCredits(credits);
        subject.setSemester(semester);
        subject.setCourseCode(courseCode.trim());
        subject.setClassOf(classOf);
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
            @RequestParam Integer classOf,
            @RequestParam Integer programId) {

        if (subjectName.isBlank() || subjectName.length() > 100
                || courseCode.isBlank() || courseCode.length() > 50
                || credits < 1 || semester < 1 || semester > 8
                || classOf < 2000 || classOf > 2100) {

            return "redirect:/admin/subjects?error=fields";
        }

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

        subject.setSubjectName(subjectName.trim());
        subject.setCredits(credits);
        subject.setSemester(semester);
        subject.setCourseCode(courseCode.trim());
        subject.setClassOf(classOf);
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
    public String adminFacultySubjects(
            @RequestParam(required = false) Integer facultyId,
            Model model) {

        model.addAttribute(
                "assignments",
                facultySubjectService.getAllAssignments()
        );

        model.addAttribute(
                "facultyMembers",
                facultyService.getAllFaculty()
        );

        model.addAttribute(
                "programs",
                programRepository.findAll()
        );

        model.addAttribute(
                "subjects",
                subjectService.getAllSubjects()
        );

        Faculty selectedFaculty = null;

        if (facultyId != null) {
            selectedFaculty = facultyService
                    .getFacultyById(facultyId)
                    .orElse(null);
        }

        model.addAttribute("selectedFaculty", selectedFaculty);

        model.addAttribute(
                "selectedFacultyId",
                selectedFaculty != null
                        ? selectedFaculty.getFacultyId()
                        : null
        );

        return "admin-faculty-subjects";
    }

    @PostMapping("/admin/faculty-subjects")
    public String addFacultySubject(
            @RequestParam Integer facultyId,
            @RequestParam Integer subjectId,
            @RequestParam String section) {

        String redirectUrl =
                "/admin/faculty-subjects?facultyId=" + facultyId;

        Faculty faculty = facultyService
                .getFacultyById(facultyId)
                .orElse(null);

        if (faculty == null) {
            return "redirect:" + redirectUrl + "&error=faculty";
        }

        Subject subject = subjectService
                .getSubjectById(subjectId)
                .orElse(null);

        if (subject == null) {
            return "redirect:" + redirectUrl + "&error=subject";
        }

        String normalizedSection =
                section == null ? "" : section.trim();

        if (normalizedSection.isBlank()
                || normalizedSection.length() > 10) {

            return "redirect:" + redirectUrl + "&error=section";
        }

        if (facultySubjectService.assignmentExists(
                facultyId,
                subjectId,
                normalizedSection)) {

            return "redirect:" + redirectUrl + "&error=duplicate";
        }

        FacultySubject assignment = new FacultySubject();

        assignment.setFaculty(faculty);
        assignment.setSubject(subject);
        assignment.setSection(normalizedSection);

        facultySubjectService.saveAssignment(assignment);

        return "redirect:" + redirectUrl + "&success";
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
