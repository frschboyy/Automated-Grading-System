package com.gradingsystem.tesla.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gradingsystem.tesla.model.Student;
import com.gradingsystem.tesla.service.StudentService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@SuppressWarnings("UnnecessaryImport")
@Controller
public class LoginController {

    @Autowired
    private StudentService studentService;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @GetMapping("/")
    public String showLoginPage(final HttpSession session, HttpServletResponse response) {
        String view;

        final Student loggedInStudent = (Student) session.getAttribute("loggedInStudent");
        final Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

        // If session exists (either student or admin is logged in), redirect to
        // dashboard
        if (loggedInStudent != null || (isAdmin != null && isAdmin)) {
            view = "redirect:/dashboard";
        } else {
            // If no session exists, show the login page
            view = "login";
        }

        // Return the view
        return view;
    }

    @PostMapping("/")
    public String handleLogin(
            @RequestParam final String username,
            @RequestParam final String password,
            final HttpSession session,
            final Model model) {

        String view;

        // Check for admin credentials
        if (adminUsername.equals(username) && adminPassword.equals(password)) {
            session.setAttribute("isAdmin", true); // Add admin session attribute
            view = "redirect:/dashboard"; // Redirect to dashboard
        } else {
            final Student student = studentService.getStudent(username);

            if (student != null && student.getPassword().equals(password)) {
                // Store the student in the session
                session.setAttribute("loggedInStudent", student);
                session.setAttribute("id", student.getId());
                view = "redirect:/dashboard"; // Redirect to dashboard
            } else {
                model.addAttribute("error", "Invalid username or password");
                view = "login";
            }
        }

        // Return the view at the end
        return view;
    }
}
