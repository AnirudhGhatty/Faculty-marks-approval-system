package com.SP.marksapproval.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/faculty-dashboard")
    public String facultyDashboard() {
        return "faculty-dashboard";
    }
    
     @GetMapping("/marks-entry")
    public String marksEntry() {
        return "marks-entry";
}
}