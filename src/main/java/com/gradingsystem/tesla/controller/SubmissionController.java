package com.gradingsystem.tesla.controller;

import com.gradingsystem.tesla.dto.EvaluationDetails;
import com.gradingsystem.tesla.dto.SubmissionDTO;
import com.gradingsystem.tesla.model.Assignment;
import com.gradingsystem.tesla.model.DocumentSubmission;
import com.gradingsystem.tesla.model.Student;
import com.gradingsystem.tesla.repository.AssignmentRepository;
import com.gradingsystem.tesla.repository.DocumentSubmissionRepository;
import com.gradingsystem.tesla.repository.StudentRepository;
import com.gradingsystem.tesla.service.CohereGradingService;
import com.gradingsystem.tesla.service.PlagiarismService;
import com.gradingsystem.tesla.service.RetrieveEvaluationService;
import com.gradingsystem.tesla.service.RetrieveSubmissions;
import com.gradingsystem.tesla.service.TextExtraction;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/submissions")
public class SubmissionController {

    private final PlagiarismService plagiarismService;
    private final CohereGradingService gradingService;
    private final RetrieveSubmissions retrieveSubmissionService;
    private final TextExtraction textExtraction;
    private final RetrieveEvaluationService retrievalService;

    private final DocumentSubmissionRepository documentSubmissionRepository;
    private final AssignmentRepository assignmentRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(SubmissionController.class);

    @Autowired
    public SubmissionController(
            final PlagiarismService plagiarismService,
            final CohereGradingService gradingService,
            final RetrieveSubmissions retrieveSubmissionService,
            final RetrieveEvaluationService retrievalService,
            final DocumentSubmissionRepository documentSubmissionRepository,
            final AssignmentRepository assignmentRepository,
            final StudentRepository studentRepository,
            final TextExtraction textExtraction) {
        this.plagiarismService = plagiarismService;
        this.gradingService = gradingService;
        this.retrieveSubmissionService = retrieveSubmissionService;
        this.retrievalService = retrievalService;
        this.documentSubmissionRepository = documentSubmissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.textExtraction = textExtraction;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<Map<String, String>> evaluateSubmission(
            @RequestParam("file") final MultipartFile file,
            final HttpSession session) {

        final Long assignmentId = (Long) session.getAttribute("assignmentId");
        final Student student = (Student) session.getAttribute("loggedInStudent");

        try {
            // Extract and hash text
            final String newSubmission = textExtraction.extractText(file);
            final String newSubmissionHash = textExtraction.generateHash(newSubmission);

            // Check for duplicate submissions
            final DocumentSubmission duplicateSubmission = documentSubmissionRepository
                    .findByAssignmentIdAndStudentIdAndHashValue(assignmentId, student.getId(), newSubmissionHash);

            if (duplicateSubmission != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Duplicate submission detected for this assignment."));
            }

            // Check for plagiarism
            double plagiarismScore = 0;
            final List<DocumentSubmission> allSubmissions = documentSubmissionRepository
                    .findByAssignmentId(assignmentId);
            for (final DocumentSubmission submission : allSubmissions) {
                // Convert extracted text to string (if it's stored as bytes or other format)
                final String existingSubmission = new String(submission.getExtractedText(), StandardCharsets.UTF_8);

                try {
                    // Calculate TF-IDF similarity using Cosine Similarity
                    final double score = plagiarismService.calculateTFIDFSimilarity(newSubmission, existingSubmission);
                    if (score > plagiarismScore) {
                        plagiarismScore = score;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // logging purposes
            if (plagiarismScore >= 0.8) { // threshold: 30%
                System.out.println("Potential plagiarism detected: " + plagiarismScore);
            }

            // Parse questions and answers
            final Map<String, String> answerToQuestion = gradingService.parseQuestionsAndAnswers(newSubmission);

            final Assignment assignment = assignmentRepository.findById(assignmentId)
                    .orElseThrow(() -> new RuntimeException("Assignment not found"));

            final byte[] rubric = assignment.getRubric();

            String rubricText;
            Map<String, String> evaluationResults;

            if (rubric != null) {
                // Evaluate based on rubric
                rubricText = new String(rubric, StandardCharsets.UTF_8);
                LOGGER.debug("enteringEvalRubric");
                evaluationResults = gradingService.evaluateAnswersWithRubric(answerToQuestion, rubricText);
            } else {
                // Evaluate without rubric
                LOGGER.debug("enteringEvalNoRubric");
                evaluationResults = gradingService.evaluateAnswersWithoutRubric(answerToQuestion);
                LOGGER.debug("results: " + evaluationResults.toString());
            }

            // Iterate through the map and print each key-value pair
            for (final Map.Entry<String, String> entry : evaluationResults.entrySet()) {
                System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
            }

            System.out.print("Entry Size: " + evaluationResults.entrySet().size());

            // Calculate student's mark for assignment
            final Integer percentage = gradingService.calculateAggregateScore(evaluationResults);

            // Save document submission
            final DocumentSubmission submission = DocumentSubmission.builder()
                    .student(student)
                    .assignment(assignment)
                    .hashValue(newSubmissionHash)
                    .extractedText(newSubmission.getBytes(StandardCharsets.UTF_8))
                    .grade(percentage)
                    .similarityScore((int) (plagiarismScore * 100))
                    .evaluationResults(evaluationResults)
                    .build();

            System.out.println("Assignment: " + assignment.getId() + ", " + assignment.getTitle() + ", "
                    + assignment.getDescription());
            System.out
                    .println("Student: " + student.getId() + ", " + student.getUsername() + ", " + student.getEmail());
            System.out.println("Grade: " + percentage);
            final DecimalFormat numberFormat = new DecimalFormat("#.00");
            System.out.println("PlagiarismScore: " + plagiarismScore);
            System.out.println("SubmissionText: " + newSubmission);
            System.out.println("HashValue: " + newSubmissionHash);
            System.out.println(
                    "SubmissionBytes: " + Arrays.toString(newSubmission.getBytes(StandardCharsets.UTF_8)) + "\n\n");

            documentSubmissionRepository.save(submission);

            final Map<String, String> responseMap = new HashMap<>();
            if (plagiarismScore < 0.8) {
                responseMap.put("message", "Submission Processed");
                responseMap.put("score", String.valueOf(percentage));
            } else {
                responseMap.put("message", "Submission Processed: Plagiarism Detected!");
                responseMap.put("Similarity Score", String.valueOf(numberFormat.format(plagiarismScore * 100)));
            }

            return ResponseEntity.ok(responseMap);

        } catch (final Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Fetch existing submissions for an assignment
    @GetMapping("/existing")
    public List<SubmissionDTO> getSubmissions(final HttpSession session) {
        final Long assignmentId = (Long) session.getAttribute("assignmentId");
        return retrieveSubmissionService.getSubmissions(assignmentId);
    }

    @PostMapping("/pushEvaluationDetails")
    public ResponseEntity<Void> saveEvaluationDetails(@RequestParam final Long assignmentId,
            @RequestParam final Long studentId,
            final HttpSession session) {

        // Fetch evaluation details
        final EvaluationDetails details = retrievalService.getEvaluationDetails(assignmentId, studentId);

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
}
