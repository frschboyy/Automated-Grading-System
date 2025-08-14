package com.gradingsystem.tesla.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.gradingsystem.tesla.dto.AssignmentDTO;
import com.gradingsystem.tesla.dto.EvaluationDTO;
import com.gradingsystem.tesla.dto.ScoreCard;
import com.gradingsystem.tesla.model.Assignment;
import com.gradingsystem.tesla.model.DocumentSubmission;
import com.gradingsystem.tesla.service.AssignmentService;
import com.gradingsystem.tesla.service.RetrieveEvaluationService;
import com.gradingsystem.tesla.service.SubmissionService;
import com.gradingsystem.tesla.util.CustomUserDetails;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class PageController {

    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final RetrieveEvaluationService retrieveEvaluationService;

    @GetMapping("/submission-page")
    public String submissionsPage(HttpSession session, Model model) {
        Long assignmentId = (Long) session.getAttribute("assignmentId");
        if (assignmentId == null) {
            return "redirect:/dashboard";
        }

        AssignmentDTO assignment = assignmentService.getAssignmentById(assignmentId);

        model.addAttribute("assignmentId", assignment.getId());
        model.addAttribute("assignmentTitle", assignment.getTitle());
        model.addAttribute("assignmentDescription", assignment.getDescription());
        model.addAttribute("dueDate", assignment.getDueDate());

        return "submissionsPage";
    }

    @GetMapping("/evaluation-page")
    public String resultsPage(HttpSession session, Model model,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Long assignmentId = (Long) session.getAttribute("assignmentId");
        if (assignmentId == null) {
            return "redirect:/dashboard";
        }

        AssignmentDTO dto = assignmentService.getAssignmentById(assignmentId);
        model.addAttribute("assignmentId", dto.getId());
        model.addAttribute("assignmentTitle", dto.getTitle());
        model.addAttribute("assignmentDescription", dto.getDescription());

        Assignment assignment = assignmentService.getAssignment(assignmentId);
        DocumentSubmission submission = submissionService.findSubmission(currentUser.getUser(), assignment);

        ScoreCard scores = submissionService.getScoreCard(submission.getId());
        model.addAttribute("scoreCard", scores); 

        // SubmissionDTO submission =
        List<EvaluationDTO> results = retrieveEvaluationService.getEvaluationData(submission.getId(), currentUser);
        model.addAttribute("results", results);

        model.addAttribute("downloadUrl", submission.getFileUrl());

        return "resultsPage";
    }
}
