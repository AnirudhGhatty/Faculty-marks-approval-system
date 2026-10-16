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

}