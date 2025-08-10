package com.gradingsystem.tesla.controller;

import com.gradingsystem.tesla.model.DocumentSubmission;
import com.gradingsystem.tesla.service.AssignmentService;
import com.gradingsystem.tesla.service.CourseService;
import com.gradingsystem.tesla.service.FirebaseStorageService;
import com.gradingsystem.tesla.service.GradingService;
import com.gradingsystem.tesla.service.MatchingService;
import com.gradingsystem.tesla.service.SubmissionParserService;
import com.gradingsystem.tesla.service.SubmissionService;
import com.gradingsystem.tesla.util.CustomUserDetails;

import jakarta.servlet.http.HttpSession;

import java.util.*;

import com.gradingsystem.tesla.util.PathUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {
    private final FirebaseStorageService firebaseStorageService;
    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final CourseService courseService;
    private final SubmissionParserService submissionParserService;
    private final GradingService gradingService;
    private final MatchingService matchingService;

    private static final Logger log = LoggerFactory.getLogger(SubmissionController.class);

    @Autowired
    public SubmissionController(FirebaseStorageService firebaseStorageService,
            AssignmentService assignmentService,
            SubmissionService submissionService,
            CourseService courseService,
            SubmissionParserService submissionParserService,
            GradingService gradingService,
            MatchingService matchingService) {
        this.firebaseStorageService = firebaseStorageService;
        this.assignmentService = assignmentService;
        this.submissionService = submissionService;
        this.courseService = courseService;
        this.submissionParserService = submissionParserService;
        this.gradingService = gradingService;
        this.matchingService = matchingService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> submitAssignment(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpSession session) {

        Long assignmentId = (Long) session.getAttribute("assignmentId");
        log.debug("Submissions - Assignment Id: {}", session.getAttribute("assignmentId"));
        Long courseId = (Long) session.getAttribute("courseId");
        log.debug("Submissions - Course Id: {}", session.getAttribute("courseId"));
        Long userId = currentUser.getUser().getId();
        log.debug("Submissions - Student Id: {}", userId);

        try {
            // Save file to Firebase
            String filePath = buildFirebasePath(assignmentId, userId, file.getOriginalFilename(), courseId);
            firebaseStorageService.uploadFile(file.getBytes(), filePath, file.getContentType());

            // Parse Submission with Cohere
            String parsedSubmissionJson = submissionParserService.parseSubmission(file);

            // Get teacher assignment JSON
            String teacherAssignmentJson = assignmentService.getAssignmentJson(Long.valueOf(assignmentId));

            // Match student answers to teacher questions
            String matchedJson = matchingService.matchQuestions(teacherAssignmentJson, parsedSubmissionJson);

            // Save submission in DB
            DocumentSubmission submission = submissionService.saveSubmission(
                    assignmentId,
                    userId,
                    filePath,
                    matchedJson);

            // Send to grading service for evaluation
            String evaluationResponse = gradingService.evaluate(matchedJson);

            // Update submission with evaluation results
            submissionService.updateEvaluation(submission.getId(), evaluationResponse);

            return ResponseEntity.ok(Map.of("message", "Submission uploaded and evaluation started"));

        } catch (Exception e) {
            log.error("Error during submission upload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Submission failed: " + e.getMessage()));
        }
    }

    private String buildFirebasePath(Long assignmentId, Long studentId, String originalFileName, Long courseId) {
        String couseCode = courseService.getCourseCode(courseId);
        return String.format("%s/%s/%s/%s_%s",
                couseCode,
                assignmentId + "_"
                        + PathUtils.sanitizePathPart(assignmentService.getAssignment(assignmentId).getTitle()),
                "submissions",
                UUID.randomUUID(),
                assignmentId + "_" + PathUtils.sanitizePathPart(originalFileName));
    }
}