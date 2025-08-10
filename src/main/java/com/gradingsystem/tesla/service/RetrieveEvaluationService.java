package com.gradingsystem.tesla.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import com.gradingsystem.tesla.dto.EvaluationDTO;
import com.gradingsystem.tesla.model.Assignment;
import com.gradingsystem.tesla.model.DocumentSubmission;
import com.gradingsystem.tesla.model.Evaluation;
import com.gradingsystem.tesla.model.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradingsystem.tesla.repository.DocumentSubmissionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RetrieveEvaluationService {

    private final DocumentSubmissionRepository submissionRepository;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public List<EvaluationDTO> getEvaluationData(Assignment assignment, User student) {
        // Find submission by student and assignment
        DocumentSubmission submission = submissionRepository.findByStudentAndAssignment(student, assignment);

        if (submission == null) {
            throw new RuntimeException("Submission not found in database");
        }

        // Get evaluation
        Evaluation evaluation = submission.getEvaluation();
        if (evaluation == null || evaluation.getEvaluationJson() == null) {
            throw new RuntimeException("Evaluation data not found for this submission");
        }

        try {
            // Parse the JSON string into a JsonNode array
            JsonNode arrayNode = objectMapper.readTree(evaluation.getEvaluationJson());
            if (!arrayNode.isArray()) {
                throw new RuntimeException("Evaluation JSON is not an array");
            }

            // Convert each node to EvaluationDTO
            List<EvaluationDTO> dtoList = new ArrayList<>();
            for (JsonNode node : arrayNode) {
                EvaluationDTO dto = EvaluationDTO.builder()
                        .id(evaluation.getId())
                        .questionNumber(node.path("questionNumber").asInt())
                        .question(node.path("question").asText())
                        .answer(node.path("answer").asText())
                        .maxScore(node.path("maxScore").asText())
                        .score(node.path("evaluation").path("score").asText())
                        .feedback(node.path("evaluation").path("feedback").asText())
                        .build();
                dtoList.add(dto);
            }

            return dtoList;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse evaluation JSON", e);
        }
    }
}
