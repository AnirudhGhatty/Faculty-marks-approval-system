package com.SP.marksapproval.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

<<<<<<< HEAD
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
}
=======
    @GetMapping("/faculty-dashboard")
    public String facultyDashboard() {
        return "faculty-dashboard";
    }
    
     @GetMapping("/marks-entry")
    public String marksEntry() {
        return "marks-entry";
}
}
>>>>>>> origin/master
