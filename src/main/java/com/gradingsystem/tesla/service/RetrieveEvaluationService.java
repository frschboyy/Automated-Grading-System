package com.gradingsystem.tesla.service;

// import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import com.gradingsystem.tesla.dto.EvaluationDTO;
// import com.gradingsystem.tesla.model.DocumentSubmission;
import com.gradingsystem.tesla.model.Evaluation;
// import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.gradingsystem.tesla.repository.DocumentSubmissionRepository;
import com.gradingsystem.tesla.repository.EvaluationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RetrieveEvaluationService {

    // private final DocumentSubmissionRepository submissionRepository;
    private final EvaluationRepository evaluationRepository;

    // private static final ObjectMapper objectMapper = new ObjectMapper();

    public List<EvaluationDTO> getEvaluationData(Long submissionId) {

        List<Evaluation> evaluations = evaluationRepository.findBySubmissionId(submissionId);
        return evaluations.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        // // Find submission by student and assignment
        // DocumentSubmission submission = submissionRepository.findById(submissionId)
        // .orElseThrow(() -> new RuntimeException("Submission not found"));

        // // Get evaluation
        // Evaluation evaluation = submission.getEvaluation();
        // if (evaluation == null || evaluation.getEvaluationJson() == null) {
        // throw new RuntimeException("Evaluation data not found for this submission");
        // }

        // try {
        // // Parse the JSON string into a JsonNode array
        // JsonNode arrayNode = objectMapper.readTree(evaluation.getEvaluationJson());
        // if (!arrayNode.isArray()) {
        // throw new RuntimeException("Evaluation JSON is not an array");
        // }

        // // Convert each node to EvaluationDTO
        // List<EvaluationDTO> dtoList = new ArrayList<>();
        // for (JsonNode node : arrayNode) {
        // EvaluationDTO dto = EvaluationDTO.builder()
        // .id(evaluation.getId())
        // .questionNumber(node.path("questionNumber").asInt())
        // .question(node.path("question").asText())
        // .answer(node.path("answer").asText())
        // .maxScore(node.path("maxScore").asText())
        // .score(node.path("evaluation").path("score").asText())
        // .feedback(node.path("evaluation").path("feedback").asText())
        // .build();
        // dtoList.add(dto);
        // }

        // return dtoList;

        // } catch (Exception e) {
        // throw new RuntimeException("Failed to parse evaluation JSON", e);
        // }
    }

    private EvaluationDTO toDTO(Evaluation e) {
        return EvaluationDTO.builder()
                .id(e.getId())
                .questionNumber(e.getQuestionNumber())
                .question(e.getQuestion())
                .answer(e.getAnswer())
                .maxScore(e.getMaxScore())
                .score(e.getScore())
                .feedback(e.getFeedback())
                .build();
    }
}
