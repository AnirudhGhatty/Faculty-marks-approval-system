package com.SP.marksapproval.controller;

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

        if (role.equals("student")) {

            // Student login logic will go here

        } else if (role.equals("faculty")) {

            // Faculty login logic will go here

        } else if (role.equals("admin")) {

            // Admin login logic will go here
        }

        return "redirect:/login";
    }
}