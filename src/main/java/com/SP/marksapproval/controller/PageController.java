
package com.SP.marksapproval.controller;

import com.SP.marksapproval.entity.Program;
import com.SP.marksapproval.entity.Student;
import com.SP.marksapproval.repository.ProgramRepository;
import com.SP.marksapproval.service.StudentService;
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

    public PageController(
            StudentService studentService,
            ProgramRepository programRepository) {
        this.studentService = studentService;
        this.programRepository = programRepository;
    }

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

    // List students
    @GetMapping("/admin/students")
    public String adminStudents(Model model) {
        model.addAttribute("students", studentService.getAllStudents());
        model.addAttribute("programs", programRepository.findAll());

        return "admin-students";
    }

    // Add student
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

    // Delete a studentt from the existing student-management page

    @PostMapping("/admin/students/delete/{rollNo}")
    public String deleteStudent(@PathVariable String rollNo) {
    Student student = studentService
            .getStudentByRollNo(rollNo)
            .orElse(null);
    if (student == null) {
        return "redirect:/admin/students?error=notfound";
    }

    studentService.deleteStudent(student.getStudentId());

    return "redirect:/admin/students?deleted";
}

    // Update a student from the existing student-management page
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

        // Preserve the existing password if the field is left blank.
        if (password != null && !password.isBlank()) {
            student.setPassword(password);
        }

        studentService.saveStudent(student);

        return "redirect:/admin/students?updated";
    }
}
