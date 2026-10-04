package com.SP.marksapproval.controller;

import com.SP.marksapproval.entity.Admin;
import com.SP.marksapproval.entity.Faculty;
import com.SP.marksapproval.entity.Student;
import com.SP.marksapproval.service.AdminService;
import com.SP.marksapproval.service.FacultyService;
import com.SP.marksapproval.service.StudentService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    private final StudentService studentService;
    private final FacultyService facultyService;
    private final AdminService adminService;

    public LoginController(
            StudentService studentService,
            FacultyService facultyService,
            AdminService adminService) {

        this.studentService = studentService;
        this.facultyService = facultyService;
        this.adminService = adminService;
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String userId,
            @RequestParam String password,
            @RequestParam String role) {

        // Student login
        if (role.equals("student")) {

            return studentService.getStudentByRollNo(userId)
                    .filter(student -> student.getPassword().equals(password))
                    .map(student -> "redirect:/student-dashboard")
                    .orElse("redirect:/login");

        }

        // Faculty login
        else if (role.equals("faculty")) {

            return facultyService.getFacultyByEmail(userId)
                    .filter(faculty -> faculty.getPassword().equals(password))
                    .map(faculty -> "redirect:/faculty-dashboard")
                    .orElse("redirect:/login");

        }

        // Admin login
        else if (role.equals("admin")) {

            return adminService.getAllAdmins()
                    .stream()
                    .filter(admin -> admin.getUsername().equals(userId))
                    .filter(admin -> admin.getPassword().equals(password))
                    .findFirst()
                    .map(admin -> "redirect:/admin-dashboard")
                    .orElse("redirect:/login");

        }

        return "redirect:/login";
    }
}