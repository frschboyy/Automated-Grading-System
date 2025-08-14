package com.gradingsystem.tesla.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradingsystem.tesla.dto.EvaluationUpdateRequest;
import com.gradingsystem.tesla.dto.ScoreCard;
import com.gradingsystem.tesla.dto.StudentDTO;
import com.gradingsystem.tesla.dto.SubmissionDTO;
import com.gradingsystem.tesla.model.Assignment;
import com.gradingsystem.tesla.model.DocumentSubmission;
import com.gradingsystem.tesla.model.Evaluation;
import com.gradingsystem.tesla.model.User;
import com.gradingsystem.tesla.repository.DocumentSubmissionRepository;
import com.gradingsystem.tesla.repository.EvaluationRepository;
import com.gradingsystem.tesla.repository.AssignmentRepository;
import com.gradingsystem.tesla.util.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final DocumentSubmissionRepository submissionRepository;
    private final EvaluationRepository evaluationRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentService assignmentService;
    private final UserService studentService;

    public DocumentSubmission saveSubmission(Long assignmentId, Long studentId, String fileUrl, String parsedJson) {
        User student = studentService.getUser(studentId);
        Assignment assignment = assignmentService.getAssignment(assignmentId);
        DocumentSubmission submission = DocumentSubmission.builder()
                .assignment(assignment)
                .student(student)
                .fileUrl(fileUrl)
                .parsedJson(parsedJson)
                .submittedAt(LocalDateTime.now())
                .build();

        return submissionRepository.save(submission);
    }

    @Transactional
    public boolean updateEvaluation(Long submissionId, int questionNumber, EvaluationUpdateRequest updateRequest, CustomUserDetails currentUser) {
        Evaluation evaluation = evaluationRepository
                .findBySubmissionIdAndQuestionNumber(submissionId, questionNumber)
                .orElseThrow(() -> new EntityNotFoundException("Evaluation not found"));

        if (!(evaluation.getSubmission().getAssignment().getCourse().getTeacher().getId().equals(currentUser.getUser().getId()))) {
            throw new AccessDeniedException("Not your course");
        }

        if (updateRequest.getScore() != null) {
            log.info("Updating score from {} to {}", evaluation.getScore(), updateRequest.getScore());

            evaluation.setScore(updateRequest.getScore());
        }
        if (updateRequest.getFeedback() != null) {
            evaluation.setFeedback(updateRequest.getFeedback());
        }
        if (updateRequest.getMaxScore() != null) {
            log.info("Updating maxScore from {} to {}", evaluation.getMaxScore(), updateRequest.getMaxScore());

            evaluation.setMaxScore(updateRequest.getMaxScore());
        }

        evaluationRepository.save(evaluation);

        DocumentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("submission not found"));

        Double oldScore = updateRequest.getOldScore() != null ? updateRequest.getOldScore() : 0.0;
        Double oldMaxScore = updateRequest.getOldMaxScore() != null ? updateRequest.getOldMaxScore() : 0.0;

        Double scoreDiff = updateRequest.getScore() - oldScore;
        Double maxScoreDiff = updateRequest.getMaxScore() - oldMaxScore;

        log.info("OldScore={} OldMaxScore={} NewScore={} NewMaxScore={} ScoreDiff={} MaxScoreDiff={}",
                oldScore, oldMaxScore, updateRequest.getScore(), updateRequest.getMaxScore(), scoreDiff, maxScoreDiff);

        // Update submission
        submission.setTotalScore(submission.getTotalScore() + scoreDiff);
        submission.setTotalMaxScore(submission.getTotalMaxScore() + maxScoreDiff);
        submission.setPercentage((submission.getTotalScore() / submission.getTotalMaxScore()));

        log.info("Updated submission totals: totalScore={} totalMaxScore={} percentage={}",
                submission.getTotalScore(), submission.getTotalMaxScore(), submission.getPercentage());

        return true;
    }

    public List<SubmissionDTO> getAllSubmissions(Long assignmentId, Long courseId, CustomUserDetails currentUser) {
        List<SubmissionDTO> submissionList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy");
        List<User> students = studentService.getStudentsForCourse(courseId, currentUser);

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        for (User student : students) {
            DocumentSubmission submission = submissionRepository.findByStudentAndAssignment(student, assignment);
            if (submission != null) {
                SubmissionDTO dto = new SubmissionDTO();
                dto.setSubmissionId(submission.getId());
                dto.setStudentId(student.getId());
                dto.setStudentRegistrationId(student.getRegistrationId());
                dto.setStudentName(
                        student.getFirstName() + " " + student.getMiddleName() + " " + student.getLastName());
                dto.setDateSubmitted(submission.getSubmittedAt().format(formatter));
                submissionList.add(dto);
            }
        }
        return submissionList;
    }

    public List<StudentDTO> getAllPendingSubmissions(Long assignmentId, Long courseId, CustomUserDetails currentUser) {
        List<StudentDTO> pendingList = new ArrayList<>();
        List<User> students = studentService.getStudentsForCourse(courseId, currentUser);

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        for (User student : students) {
            DocumentSubmission submission = submissionRepository.findByStudentAndAssignment(student, assignment);
            if (submission == null) {
                StudentDTO dto = new StudentDTO();
                dto.setRegistrationId(student.getRegistrationId());
                dto.setName(student.getFirstName() + " " + student.getMiddleName() + " " + student.getLastName());
                dto.setEmail(student.getEmail());
                pendingList.add(dto);
            }
        }
        return pendingList;
    }

    public DocumentSubmission findSubmission(User user, Assignment assignment) {
        return submissionRepository.findByStudentAndAssignment(user, assignment);
    }

    public void migrateJson(DocumentSubmission submission, String evaluationJson) throws Exception {
        double totalScore = 0;
        double totalMaxScore = 0;

        JsonNode arrayNode = new ObjectMapper().readTree(evaluationJson);
        if (!arrayNode.isArray()) {
            throw new RuntimeException("Evaluation JSON not array");
        }

        List<Evaluation> evaluations = new ArrayList<>();
        for (JsonNode node : arrayNode) {
            totalScore += node.path("evaluation").path("score").asDouble();
            totalMaxScore += node.path("maxScore").asDouble();
            evaluations.add(Evaluation.builder()
                    .questionNumber(node.path("questionNumber").asInt())
                    .question(node.path("question").asText())
                    .answer(node.path("answer").asText())
                    .maxScore(node.path("maxScore").asDouble())
                    .score(node.path("evaluation").path("score").asDouble())
                    .feedback(node.path("evaluation").path("feedback").asText())
                    .submission(submission)
                    .build());
        }

        // Update submission
        submission.setTotalScore(totalScore);
        submission.setTotalMaxScore(totalMaxScore);
        submission.setPercentage((totalScore / totalMaxScore) * 100);

        // Save evaluations
        evaluationRepository.saveAll(evaluations);
    }

    public String getSubmissionUrl(Long submissionId) {
        DocumentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
        return submission.getFileUrl();
    }

    public ScoreCard getScoreCard(Long submissionId) {
        DocumentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
        ScoreCard card = ScoreCard.builder()
                .score(submission.getTotalScore())
                .maxScore(submission.getTotalMaxScore())
                .percentage(submission.getPercentage())
                .build();

        return card;
    }
}
