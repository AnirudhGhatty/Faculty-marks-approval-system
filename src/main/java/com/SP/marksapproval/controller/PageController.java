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
            subjectService.getSubjectsByProgramAndSemester(
                    student.getProgram().getProgramId(),
                    student.getSemester());

    List<NcpMarks> ncpRecords =
            ncpMarksService.getNcpMarksByStudentId(
                    student.getStudentId());

    Map<Integer, Integer> externalMarksBySubject = new java.util.HashMap<>();

    for (NcpMarks record : ncpRecords) {

        int total = 0;
        boolean hasSubmittedMarks = false;

        if (Boolean.TRUE.equals(record.getNcp1Locked())
                && record.getNcp1() != null) {
            total += record.getNcp1();
            hasSubmittedMarks = true;
        }

        if (Boolean.TRUE.equals(record.getNcp2Locked())
                && record.getNcp2() != null) {
            total += record.getNcp2();
            hasSubmittedMarks = true;
        }

        if (Boolean.TRUE.equals(record.getNcp3Locked())
                && record.getNcp3() != null) {
            total += record.getNcp3();
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
            subjectService.getSubjectsByProgramAndSemester(
                    student.getProgram().getProgramId(),
                    student.getSemester());

    List<NcpMarks> ncpRecords =
            ncpMarksService.getNcpMarksByStudentId(
                    student.getStudentId());

    Map<Integer, NcpMarks> ncpMarksBySubject = new java.util.HashMap<>();

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

    // Dynamic dashboard statistics
    model.addAttribute("totalStudents",
            studentService.getAllStudents().size());

    model.addAttribute("totalFaculty",
            facultyService.getAllFaculty().size());

    model.addAttribute("totalSubjects",
            subjectService.getAllSubjects().size());

    // Faculty-subject-section assignments
    List<FacultySubject> assignments =
            facultySubjectService.getAllAssignments();

    List<Map<String, Object>> dashboardRows =
            new java.util.ArrayList<>();

    for (FacultySubject assignment : assignments) {

        Subject subject = assignment.getSubject();

        List<Student> students =
                studentService.getStudentsForSubjectSection(
                        subject,
                        assignment.getSection());

        List<NcpMarks> records =
                ncpMarksService.getNcpMarksBySubjectId(
                        subject.getSubjectId());

        // Keep only records belonging to this assignment's section
        java.util.Set<Integer> studentIds = students.stream()
                .map(Student::getStudentId)
                .collect(Collectors.toSet());

        List<NcpMarks> relevantRecords = records.stream()
                .filter(record -> studentIds.contains(
                        record.getStudent().getStudentId()))
                .collect(Collectors.toList());

        Map<String, Object> row = new java.util.HashMap<>();

        row.put("facultyName", assignment.getFaculty().getName());
        row.put("subjectName", subject.getSubjectName());
        row.put("section", assignment.getSection());
        row.put("studentCount", students.size());

        row.put("ncp1Status",
                getNcpApprovalStatus(relevantRecords, students.size(), "NCP1"));

        row.put("ncp2Status",
                getNcpApprovalStatus(relevantRecords, students.size(), "NCP2"));

        row.put("ncp3Status",
                getNcpApprovalStatus(relevantRecords, students.size(), "NCP3"));

        dashboardRows.add(row);
    }

    model.addAttribute("dashboardRows", dashboardRows);

    return "admin-dashboard";
}

/**
 * Calculates the overall approval status for an NCP
 * across all students in one faculty-subject-section assignment.
 */
private String getNcpApprovalStatus(
        List<NcpMarks> records,
        int totalStudents,
        String ncp) {

    if (totalStudents == 0) {
        return "Not Submitted";
    }

    int submittedCount = 0;
    int approvedCount = 0;

    for (NcpMarks record : records) {

        boolean locked = false;
        boolean approved = false;
        Integer score = null;

        switch (ncp) {
            case "NCP1":
                locked = Boolean.TRUE.equals(record.getNcp1Locked());
                approved = Boolean.TRUE.equals(record.getNcp1Approved());
                score = record.getNcp1();
                break;

            case "NCP2":
                locked = Boolean.TRUE.equals(record.getNcp2Locked());
                approved = Boolean.TRUE.equals(record.getNcp2Approved());
                score = record.getNcp2();
                break;

            case "NCP3":
                locked = Boolean.TRUE.equals(record.getNcp3Locked());
                approved = Boolean.TRUE.equals(record.getNcp3Approved());
                score = record.getNcp3();
                break;
        }

        if (locked && score != null) {
            submittedCount++;

            if (approved) {
                approvedCount++;
            }
        }
    }

    if (approvedCount == totalStudents) {
        return "Approved";
    }

    if (submittedCount > 0) {
        return "Pending Approval";
    }

    return "Not Submitted";
}

    
@GetMapping("/faculty-dashboard")
public String facultyDashboard(
        Authentication authentication,
        Model model) {

    // Authentication check
    if (authentication == null) {
        return "redirect:/login";
    }

    String[] loginParts = authentication.getName().split(":", 2);

    if (loginParts.length != 2
            || !"faculty".equalsIgnoreCase(loginParts[0])) {
        return "redirect:/faculty-dashboard";
    }

    // Fetch faculty
    Faculty faculty = facultyService
            .getFacultyByEmail(loginParts[1])
            .orElse(null);

    if (faculty == null) {
        return "redirect:/login";
    }

    // Fetch faculty assignments
    List<FacultySubject> assignments =
            facultySubjectService.getAssignmentsByFaculty(
                    faculty.getFacultyId());

    // Dashboard calculations
    java.util.Set<Integer> uniqueStudentIds =
            new java.util.HashSet<>();

    int ncp1Pending = 0;
    int ncp2Pending = 0;
    int ncp3Pending = 0;

    for (FacultySubject assignment : assignments) {

        List<Student> students =
                studentService.getStudentsForSubjectSection(
                        assignment.getSubject(),
                        assignment.getSection());

        // Count unique students across all assigned sections
        for (Student student : students) {
            uniqueStudentIds.add(student.getStudentId());
        }

        // Fetch marks for the assigned subject
        List<NcpMarks> records =
                ncpMarksService.getNcpMarksBySubjectId(
                        assignment.getSubject().getSubjectId());

        // Keep only records belonging to students in this assignment
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

        // Calculate pending submissions independently
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

    // Send dashboard values to Thymeleaf
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
