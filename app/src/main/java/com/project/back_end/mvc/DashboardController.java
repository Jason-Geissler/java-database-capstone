package com.project.back_end.mvc;

import com.project.back_end.services.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class DashboardController {

    private final SharedService sharedService;

    @Autowired
    public DashboardController(SharedService sharedService) {
        this.sharedService = sharedService;
    }

    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable("token") String token) {
        boolean valid = sharedService.validateToken(token, "admin");
        if (valid) {
            return "admin/adminDashboard"; // forwards to Thymeleaf or JSP view
        } else {
            return "redirect:/"; // redirect to login/home if invalid
        }
    }

    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable("token") String token) {
        boolean valid = sharedService.validateToken(token, "doctor");
        if (valid) {
            return "doctor/doctorDashboard"; // forwards to doctor view
        } else {
            return "redirect:/";
        }
    }
}
