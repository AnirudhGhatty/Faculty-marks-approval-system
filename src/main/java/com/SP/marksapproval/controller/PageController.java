
package com.SP.marksapproval.controller;

import com.SP.marksapproval.entity.Faculty;
import com.SP.marksapproval.entity.Program;
import com.SP.marksapproval.entity.Student;
import com.SP.marksapproval.entity.Subject;
import com.SP.marksapproval.repository.ProgramRepository;
import com.SP.marksapproval.service.FacultyService;
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

    public PageController(
            StudentService studentService,
            ProgramRepository programRepository,
            FacultyService facultyService,
            SubjectService subjectService) {

        this.studentService = studentService;
        this.programRepository = programRepository;
        this.facultyService = facultyService;
        this.subjectService = subjectService;
    }

    // LOGIN AND DASHBOARDS

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
    public String facultyDashboard() {
        return "faculty-dashboard";
    }

    @GetMapping("/marks-entry")
    public String marksEntry() {
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
        model.addAttribute("facultyMembers", facultyService.getAllFaculty());
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

    // =========================
    // SUBJECT MANAGEMENT
    // =========================

    @GetMapping("/admin/subjects")
    public String adminSubjects(Model model) {
        model.addAttribute("subjects", subjectService.getAllSubjects());
        model.addAttribute("programs", programRepository.findAll());
        return "admin-subjects";
    }

    // Add subject
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

    // Update subject
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

    // Delete subject
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
}
