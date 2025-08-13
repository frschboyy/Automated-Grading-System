package com.gradingsystem.tesla.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gradingsystem.tesla.model.DocumentSubmission;
import com.gradingsystem.tesla.service.AssignmentService;
import com.gradingsystem.tesla.service.CourseService;
import com.gradingsystem.tesla.service.FirebaseStorageService;
import com.gradingsystem.tesla.service.GradingService;
import com.gradingsystem.tesla.service.MatchingService;
import com.gradingsystem.tesla.service.SubmissionParserService;
import com.gradingsystem.tesla.service.SubmissionService;
import com.gradingsystem.tesla.util.CustomUserDetails;
import com.gradingsystem.tesla.util.PathUtils;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final FirebaseStorageService firebaseStorageService;
    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final CourseService courseService;
    private final SubmissionParserService submissionParserService;
    private final GradingService gradingService;
    private final MatchingService matchingService;

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
            String teacherAssignmentJson = assignmentService.getAssignmentJson(assignmentId);

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

            // Parse Json string and save in evaluation db
            submissionService.migrateJson(submission, evaluationResponse);

            return ResponseEntity.ok(Map.of("message", "Submission uploaded and evaluation started"));

        } catch (Exception e) {
            log.error("Error during submission upload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Submission failed: " + e.getMessage()));
        }
    }

    private String buildFirebasePath(Long assignmentId, Long studentId, String originalFileName, Long courseId) {
        String courseCode = courseService.getCourseCode(courseId);
        return String.format("%s/%s/%s/%s_%s",
                courseCode,
                assignmentId + "_"
                + PathUtils.sanitizePathPart(assignmentService.getAssignment(assignmentId).getTitle()),
                "submissions",
                studentId.toString(),
                assignmentId + "_" + PathUtils.sanitizePathPart(originalFileName));
    }
}
