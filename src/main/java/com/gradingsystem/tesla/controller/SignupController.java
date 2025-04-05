package com.gradingsystem.tesla.controller;

import com.gradingsystem.tesla.model.Student;
import com.gradingsystem.tesla.service.StudentService;
import jakarta.servlet.http.HttpSession;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SignupController {

    @Autowired
    private StudentService studentService;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    public SignupController() {
    }

    @GetMapping("/signup")
    public String showSignupPage(final HttpSession session, final Model model) {
        String view;

        final Student loggedInStudent = (Student) session.getAttribute("loggedInStudent");
        final Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

        if (isAdmin != null && isAdmin || loggedInStudent != null) {
            // If admin or student session exists, redirect to the dashboard
            view = "redirect:/dashboard";
        } else {
            model.addAttribute("student", new Student());
            view = "signup";
        }

        return view;
    }

    @PostMapping("/signup")
    public String handleSignup(@ModelAttribute("student") final Student student, final HttpSession session) {
        String redirectUrl;

        if (student.getUsername() == null || student.getPassword() == null) {
            redirectUrl = "redirect:/signup?error=Invalid input";
        } else if (!validateEmail(student.getEmail())) {
            redirectUrl = "redirect:/signup?error=Invalid email";
        } else {
            try {
                final Student added = studentService.saveStudent(student);

                if (added != null) {
                    // Store the student in the session
                    session.setAttribute("loggedInStudent", student);
                    session.setAttribute("id", student.getId());
                    redirectUrl = "redirect:/dashboard";
                } else {
                    // If student is not added, redirect with an error
                    redirectUrl = "redirect:/signup?error=Failed to create account";
                }
            } catch (DataIntegrityViolationException e) {
                // Handle case where email already exists or other data integrity issues
                redirectUrl = "redirect:/signup?error=Email already exists!";
            } catch (Exception e) {
                e.printStackTrace(); // Log other unexpected errors
                redirectUrl = "redirect:/signup?error=Unexpected error occurred";
            }
        }

        return redirectUrl;
    }

    private boolean validateEmail(final String email) {
        boolean isValid = true;

        if (email == null || email.trim().isEmpty()) {
            isValid = false;
        } else {
            final Matcher matcher = EMAIL_PATTERN.matcher(email);
            if (!matcher.matches()) {
                isValid = false;
            }
        }

        return isValid;
    }
}
