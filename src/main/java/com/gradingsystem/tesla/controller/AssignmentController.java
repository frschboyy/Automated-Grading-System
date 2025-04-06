package com.gradingsystem.tesla.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.gradingsystem.tesla.dto.AssignmentDTO;
import com.gradingsystem.tesla.dto.EvaluationDetails;
import com.gradingsystem.tesla.model.Assignment;
import com.gradingsystem.tesla.service.AssignmentService;
import com.gradingsystem.tesla.service.RetrieveEvaluationService;
import com.gradingsystem.tesla.service.TextExtraction;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final TextExtraction textExtraction;
    private final AssignmentService assignmentService;
    private final RetrieveEvaluationService retrievalService;

    private static final Logger LOGGER = LoggerFactory.getLogger(AssignmentController.class);

    @Autowired
    public AssignmentController(
            final AssignmentService assignmentService,
            final RetrieveEvaluationService retrievalService,
            final TextExtraction textExtraction) {
        this.assignmentService = assignmentService;
        this.retrievalService = retrievalService;
        this.textExtraction = textExtraction;
    }

    // Fetch unsubmitted assignments
    @GetMapping("/upcoming")
    public List<Assignment> getUnsubmittedAssignments(final HttpSession session) {
        final Long studentId = (Long) session.getAttribute("id");
        LOGGER.debug("bbbb");
        return assignmentService.getUpcomingAssignments(studentId);
    }

    // Fetch submitted assignments
    @GetMapping("/submitted")
    public List<Assignment> getSubmittedAssignments(final HttpSession session) {
        final Long studentId = (Long) session.getAttribute("id");
        return assignmentService.getSubmittedAssignments(studentId);
    }

    // Fetch all assignments
    @GetMapping("/all")
    public List<Assignment> getAllAssignments() {
        return assignmentService.getAllAssignments();
    }

    // Endpoint to create a new assignment
    @PostMapping
    public ResponseEntity<String> createAssignment(
            @RequestParam("assignmentName") final String assignmentName,
            @RequestParam("dueDate") final String dueDate,
            @RequestParam("description") final String description,
            @RequestParam(value = "uploadFile", required = false) final MultipartFile file) {

        // Declare a variable to store the ResponseEntity
        ResponseEntity<String> response;

        // Validate fields
        if (assignmentName == null || dueDate == null || description == null) {
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid request: Missing required fields");
        } else {
            try {
                final LocalDateTime dueDateTime = LocalDateTime.parse(dueDate);

                // Create the assignment
                final Assignment assignment = Assignment.builder()
                        .description(description)
                        .dueDate(dueDateTime)
                        .title(assignmentName)
                        .build();

                // Handle optional file
                if (file != null && !file.isEmpty()) {
                    final String extractedRubric = textExtraction.extractText(file);
                    System.out.println("proposed rubric: " + extractedRubric);
                    assignment.setRubric(extractedRubric.getBytes(StandardCharsets.UTF_8));
                } else {
                    assignment.setRubric(null);
                }

                System.out.println("new rubric: " + Arrays.toString(assignment.getRubric()));

                // Save the assignment
                final Assignment createdAssignment = assignmentService.createAssignment(assignment);

                response = ResponseEntity.status(HttpStatus.CREATED)
                        .body("Assignment '" + createdAssignment.getTitle() + "' added successfully");

            } catch (DateTimeParseException ex) {
                response = ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid due date format: " + ex.getMessage());
            } catch (IOException ex) {
                response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error processing file: " + ex.getMessage());
            } catch (Exception ex) {
                response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error creating assignment: " + ex.getMessage());
            }
        }

        // Return the response at the end of the method
        return response;
    }

    @PostMapping("/pushAssignmentDetails")
    public ResponseEntity<Void> saveAssignmentDetails(@RequestBody final AssignmentDTO details, final HttpSession session) {

        System.out.println(details);
        System.out.println(details.getId());
        System.out.println(details.getTitle());
        System.out.println(details.getDescription());
        System.out.println(details.getDueDate());

        // Save data
        session.setAttribute("assignmentId", details.getId());
        session.setAttribute("title", details.getTitle());
        session.setAttribute("description", details.getDescription());
        session.setAttribute("dueDate", details.getDueDate());
        System.out.println("Added to session");

        // Return HTTP 200 - OK response
        return ResponseEntity.ok().build();
    }

    @PostMapping("/pushEvaluationDetails")
    public ResponseEntity<Void> saveEvaluationDetails(@RequestParam final Long assignmentId, final HttpSession session) {
        // Fetch evaluation details
        final EvaluationDetails details = retrievalService.getEvaluationDetails(assignmentId,
                (Long) session.getAttribute("id"));

        // Save data
        session.setAttribute("grade", details.getGrade());
        session.setAttribute("plagiarism", details.getPlagiarismScore());
        session.setAttribute("results", details.getResults());
        LOGGER.debug("Added to session");
        LOGGER.debug("Evaluation Details 123:" + details.getResults());
        System.out.println("Added to session");

        // Return HTTP 200 - OK response
        return ResponseEntity.ok().build();
    }
    // deleteMapping for the assignm,
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteAssignment(@PathVariable Long id) {
        try {
            boolean deleted = assignmentService.deleteAssignmentById(id);

            if (deleted) {
                return ResponseEntity.ok("Assignment deleted successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Assignment not found.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting assignment: " + e.getMessage());
        }
    }
}


