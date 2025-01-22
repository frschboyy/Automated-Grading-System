package com.gradingsystem.tesla.controller;

import com.gradingsystem.tesla.model.Student;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        Student loggedInStudent = (Student) session.getAttribute("loggedInStudent");
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

        logger.debug("student: " + loggedInStudent + ", admin: " + isAdmin);

        // Redirect to login if not authenticated
        if (loggedInStudent == null && isAdmin == null) {
            return "redirect:/";
        }

        if (isAdmin == null) {
            // Pass the username to the dashboard
            model.addAttribute("username", loggedInStudent.getUsername());
            return "dashboard";
        }
        return "adminDashboard";
    }

    @GetMapping("/logout")
    public String handleLogout(HttpSession session) {
        session.invalidate(); // Clear the session
        return "redirect:/";
    }

    @GetMapping("/assignment-creation")
    public String showAddAssignmentPage(HttpSession session) {
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin != null && isAdmin) {
            return "addAssignment"; // Return the add-assignment view
        }

        return "redirect:/"; // Redirect to login if not authenticated as admin
    }

    @GetMapping("/submissions-page")
    public String getSubmissionsPage(Model model, HttpSession session) {
        Student loggedInStudent = (Student) session.getAttribute("loggedInStudent");
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

        // Redirect to login if not authenticated
        if (loggedInStudent == null && isAdmin == null) {
            return "redirect:/";
        }

        // Add assignment details to the model
        model.addAttribute("id", (Long) session.getAttribute("assignmentId"));
        model.addAttribute("title", (String) session.getAttribute("title"));
        model.addAttribute("description", (String) session.getAttribute("description"));
        model.addAttribute("dueDate", session.getAttribute("dueDate"));

        if (isAdmin == null) {
            return "submitAssignmentPage";
        }
        return "submissionsPage";
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/evaluation-page")
    public String getEvaluationPage(Model model, HttpSession session) {

        // Redirect to login if not authenticated
        if ((Student) session.getAttribute("loggedInStudent") == null) {
            return "redirect:/";
        }

        // Add assignment details to the model
        model.addAttribute("grade", (Integer) session.getAttribute("grade"));
        model.addAttribute("plagiarism", (Integer) session.getAttribute("plagiarism"));
        model.addAttribute("results", (Map<String, String>) session.getAttribute("results"));

        // Return the view
        return "resultsPage";
    }
}
