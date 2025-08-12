package com.gradingsystem.tesla.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradingsystem.tesla.dto.EvaluationUpdateRequest;
import com.gradingsystem.tesla.dto.StudentDTO;
import com.gradingsystem.tesla.dto.SubmissionDTO;
import com.gradingsystem.tesla.model.Assignment;
import com.gradingsystem.tesla.model.DocumentSubmission;
import com.gradingsystem.tesla.model.Evaluation;
import com.gradingsystem.tesla.model.User;
import com.gradingsystem.tesla.repository.DocumentSubmissionRepository;
import com.gradingsystem.tesla.repository.EvaluationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final DocumentSubmissionRepository submissionRepository;
    private final EvaluationRepository evaluationRepository;
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
    public boolean updateEvaluation(Long submissionId, int questionNumber, EvaluationUpdateRequest updateRequest) {
        Optional<Evaluation> eval = evaluationRepository.findBySubmissionIdAndQuestionNumber(submissionId,
                questionNumber);
        if (eval.isEmpty()) {
            return false;
        }
        Evaluation evaluation = eval.get();

        if (updateRequest.getScore() != null) {
            evaluation.setScore(updateRequest.getScore());
        }
        if (updateRequest.getFeedback() != null) {
            evaluation.setFeedback(updateRequest.getFeedback());
        }
        if (updateRequest.getMaxScore() != null) {
            evaluation.setMaxScore(updateRequest.getMaxScore());
        }

        evaluationRepository.save(evaluation);
        return true;
    }

    public List<SubmissionDTO> getAllSubmissions(Long assignmentId, Long courseId) {
        List<SubmissionDTO> submissionList = new ArrayList<SubmissionDTO>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy");
        List<User> students = studentService.getStudentsForCourse(courseId);
        for (User student : students) {
            DocumentSubmission submission = submissionRepository.findByStudent(student);
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

    public List<StudentDTO> getAllPendingSubmissions(Long assignmentId, Long courseId) {
        List<StudentDTO> pendingList = new ArrayList<StudentDTO>();
        List<User> students = studentService.getStudentsForCourse(courseId);
        for (User student : students) {
            DocumentSubmission submission = submissionRepository.findByStudent(student);
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

    public void migrateJson(Long submissionId, String evaluationJson) throws Exception {
        DocumentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        JsonNode arrayNode = new ObjectMapper().readTree(evaluationJson);
        if (!arrayNode.isArray())
            throw new RuntimeException("Evaluation JSON not array");

        List<Evaluation> evaluations = new ArrayList<>();
        for (JsonNode node : arrayNode) {
            evaluations.add(Evaluation.builder()
                    .questionNumber(node.path("questionNumber").asInt())
                    .question(node.path("question").asText())
                    .answer(node.path("answer").asText())
                    .maxScore(node.path("maxScore").asInt())
                    .score(node.path("evaluation").path("score").asInt())
                    .feedback(node.path("evaluation").path("feedback").asText())
                    .submission(submission)
                    .build());
        }

        evaluationRepository.saveAll(evaluations);
    }
}
