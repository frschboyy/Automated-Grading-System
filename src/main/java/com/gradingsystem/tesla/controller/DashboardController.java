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

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardController.class);

    @SuppressWarnings("null")
    @GetMapping("/dashboard")
    public String showDashboard(final HttpSession session, final Model model) {
        final Student loggedInStudent = (Student) session.getAttribute("loggedInStudent");
        final Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

        LOGGER.debug("student: " + loggedInStudent + ", admin: " + isAdmin);

        // Initialize the view variable to control which view to return
        String view;

        // Redirect to login if not authenticated
        if (loggedInStudent == null && isAdmin == null) {
            view = "redirect:/";
        } else if (isAdmin == null) {
            // Pass the username to the dashboard
            model.addAttribute("username", loggedInStudent.getUsername());
            view = "dashboard";
        } else {
            view = "adminDashboard";
        }

        return view;
    }

    @GetMapping("/logout")
    public String handleLogout(final HttpSession session) {
        session.invalidate(); // Clear the session
        return "redirect:/";
    }

    @GetMapping("/assignment-creation")
    public String showAddAssignmentPage(final HttpSession session) {
        final Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

        // Initialize the view variable to control the return value
        String view;

        if (isAdmin != null && isAdmin) {
            view = "addAssignment"; // Return the add-assignment view
        } else {
            view = "redirect:/"; // Redirect to login if not authenticated as admin
        }

        return view;
    }

    @GetMapping("/submissions-page")
    public String getSubmissionsPage(final Model model, final HttpSession session) {
        final Student loggedInStudent = (Student) session.getAttribute("loggedInStudent");
        final Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

        // Initialize the view variable to control the return value
        String view;

        // Redirect to login if not authenticated
        if (loggedInStudent == null && isAdmin == null) {
            view = "redirect:/";
        } else {
            // Add assignment details to the model
            model.addAttribute("id", (Long) session.getAttribute("assignmentId"));
            model.addAttribute("title", (String) session.getAttribute("title"));
            model.addAttribute("description", (String) session.getAttribute("description"));
            model.addAttribute("dueDate", session.getAttribute("dueDate"));

            if (isAdmin == null) {
                view = "submitAssignmentPage";
            } else {
                view = "submissionsPage";
            }
        }

        return view;
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/evaluation-page")
    public String getEvaluationPage(final Model model, final HttpSession session) {
        // Initialize the view variable to control the return value
        String view;

        // Redirect to login if not authenticated
        if ((Student) session.getAttribute("loggedInStudent") == null) {
            view = "redirect:/";
        } else {
            // Add assignment details to the model
            model.addAttribute("grade", (Integer) session.getAttribute("grade"));
            model.addAttribute("plagiarism", (Integer) session.getAttribute("plagiarism"));
            model.addAttribute("results", (Map<String, String>) session.getAttribute("results"));

            // Set the view name
            view = "resultsPage";
        }

        // Return the view
        return view;
    }
}
