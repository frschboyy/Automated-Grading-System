package com.gradingsystem.tesla.controller;

import com.gradingsystem.tesla.dto.AssignmentDTO;
import com.gradingsystem.tesla.dto.CourseDTO;
import com.gradingsystem.tesla.service.AssignmentService;
import com.gradingsystem.tesla.service.UserService;
import com.gradingsystem.tesla.util.CustomUserDetails;

import jakarta.servlet.http.HttpSession;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class DashboardController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardController.class);

    private final UserService studentService;
    private final AssignmentService assignmentService;

    public DashboardController(UserService studentService,
            AssignmentService assignmentService) {
        this.studentService = studentService;
        this.assignmentService = assignmentService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        if (currentUser == null) {
            return "redirect:/";
        }

        String userRole = currentUser.getUser().getRole();
        LOGGER.debug("Logged in user: {}, Role: {}", currentUser.getUsername(), userRole);

        model.addAttribute("username",
                currentUser.getUser().getFirstName() + " " + currentUser.getUser().getLastName());

        switch (userRole) {
            case "ADMIN":
                return "adminDashboard";
            case "INSTITUTION_ADMIN":
                return "institutionAdminDashboard";
            case "TEACHER":
                return "teacherDashboard";
            case "STUDENT":
                return "redirect:/student-dashboard";
            default:
                return "redirect:/student-dashboard";
        }
    }

    @GetMapping("/student-dashboard")
    public String getDashboard(@AuthenticationPrincipal CustomUserDetails currentUser,
            HttpSession session,
            Model model) {
        Long studentId = currentUser.getUser().getId();

        // fetch enrolled courses
        List<CourseDTO> courses = studentService.getCoursesForStudent(studentId);
        model.addAttribute("courses", courses);

        return "dashboard";
    }

    @GetMapping("/student-dashboard/assignments")
    public String getAssignmentsForCourse(@AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam Long courseId,
            Model model,
            HttpSession session) {
        Long studentId = currentUser.getUser().getId();
        session.setAttribute("courseId", courseId);

        List<AssignmentDTO> upcomingAssignments = assignmentService.getUpcomingAssignmentsForStudentAndCourse(studentId,
                courseId);
        List<AssignmentDTO> submittedAssignments = assignmentService
                .getSubmittedAssignmentsForStudentAndCourse(studentId, courseId);

        // Log fields of each upcoming assignment DTO
        upcomingAssignments.forEach(dto -> {
            LOGGER.info("Upcoming Assignment - Title: {}, URL: {}",
                    dto.getTitle(), dto.getAssignmentFileUrl());
        });

        model.addAttribute("upcomingAssignments", upcomingAssignments);
        model.addAttribute("submittedAssignments", submittedAssignments);

        return "fragments/assignments :: assignmentsFragment";
    }

    @GetMapping("/select-assignment")
    public String selectAssignment(@RequestParam Long assignmentId, HttpSession session) {
        session.setAttribute("assignmentId", assignmentId);
        LOGGER.debug("set_assignmentId: {}", (Long) session.getAttribute("assignmentId"));
        return "redirect:/submission-page";
    }

    @GetMapping("/select-submission")
    public String selectSubmission(@RequestParam Long assignmentId, HttpSession session) {
        session.setAttribute("assignmentId", assignmentId);
        LOGGER.debug("set_assignmentId: {}", (Long) session.getAttribute("assignmentId"));
        return "redirect:/evaluation-page";
    }

    @GetMapping("/logout")
    public String handleLogout() {
        // Spring Security handles session invalidation
        return "redirect:/";
    }

    @GetMapping("/assignment-creation")
    public String showAddAssignmentPage(@AuthenticationPrincipal CustomUserDetails currentUser) {
        if (currentUser != null && "TEACHER".equals(currentUser.getUser().getRole())) {
            return "addAssignment";
        }
        return "redirect:/";
    }
}
