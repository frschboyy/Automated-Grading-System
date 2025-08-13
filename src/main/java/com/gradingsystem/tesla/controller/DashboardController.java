package com.gradingsystem.tesla.controller;

import com.gradingsystem.tesla.dto.AssignmentDTO;
import com.gradingsystem.tesla.dto.CourseDTO;
import com.gradingsystem.tesla.dto.EvaluationDTO;
import com.gradingsystem.tesla.dto.StudentDTO;
import com.gradingsystem.tesla.dto.SubmissionDTO;
import com.gradingsystem.tesla.model.Assignment;
import com.gradingsystem.tesla.service.AssignmentService;
import com.gradingsystem.tesla.service.RetrieveEvaluationService;
import com.gradingsystem.tesla.service.SubmissionService;
import com.gradingsystem.tesla.service.UserService;
import com.gradingsystem.tesla.util.CustomUserDetails;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserService studentService;
    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final RetrieveEvaluationService retrieveEvaluationService;

    @GetMapping("/dashboard")
    public String showDashboard(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        if (currentUser == null) {
            return "redirect:/";
        }

        String userRole = currentUser.getUser().getRole();
        log.debug("Logged in user: {}, Role: {}", currentUser.getUsername(), userRole);

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
            log.info("Upcoming Assignment - Title: {}, URL: {}",
                    dto.getTitle(), dto.getAssignmentFileUrl());
        });

        model.addAttribute("upcomingAssignments", upcomingAssignments);
        model.addAttribute("submittedAssignments", submittedAssignments);

        return "fragments/assignments :: assignmentsFragment";
    }

    @GetMapping("/select-assignment")
    public String selectAssignment(@RequestParam Long assignmentId, HttpSession session) {
        session.setAttribute("assignmentId", assignmentId);
        log.debug("set_assignmentId: {}", (Long) session.getAttribute("assignmentId"));
        return "redirect:/submission-page";
    }

    @GetMapping("/select-submission")
    public String selectSubmission(@RequestParam Long assignmentId, HttpSession session) {
        session.setAttribute("assignmentId", assignmentId);
        log.debug("set_assignmentId: {}", (Long) session.getAttribute("assignmentId"));
        return "redirect:/evaluation-page";
    }

    // TEACHER
    // Query all submissions for an assignment
    @GetMapping("teacher/assignments/{assignmentId}/submissions")
    public String getSubmissions(@PathVariable Long assignmentId,
            Model model,
            HttpSession session) {

        session.setAttribute("assignmentId", assignmentId);

        Long courseId = (Long) session.getAttribute("courseId");
        List<SubmissionDTO> submissions = submissionService.getAllSubmissions(assignmentId, courseId);
        List<StudentDTO> pendingSubmissions = submissionService.getAllPendingSubmissions(assignmentId, courseId);

        model.addAttribute("submission", submissions);
        model.addAttribute("pendingSubmission", pendingSubmissions);

        return "submissionList";
    }

    // Query evaluation for a student
    @GetMapping("teacher/submissions/{submissionId}")
    public String getEvaluation(@PathVariable Long submissionId,
            Model model,
            HttpSession session) {
        session.setAttribute("submissionId", submissionId);

        Long assignmentId = (Long) session.getAttribute("assignmentId");

        AssignmentDTO dto = assignmentService.getAssignmentById(assignmentId);
        model.addAttribute("assignmentId", dto.getId());
        model.addAttribute("assignmentTitle", dto.getTitle());
        model.addAttribute("assignmentDescription", dto.getDescription());

        // Assignment assignment = assignmentService.getAssignment(assignmentId);
        String downloadUrl = submissionService.getSubmissionUrl(submissionId);
        model.addAttribute("downloadUrl", downloadUrl);

        List<EvaluationDTO> results = retrieveEvaluationService.getEvaluationData(submissionId);

        model.addAttribute("results", results);

        return "evaluationPage";
    }

    @GetMapping("/assignment-creation")
    public String showAddAssignmentPage(@AuthenticationPrincipal CustomUserDetails currentUser) {
        if (currentUser != null && "TEACHER".equals(currentUser.getUser().getRole())) {
            return "addAssignment";
        }
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String handleLogout() {
        // Spring Security handles session invalidation
        return "redirect:/";
    }

}
