package com.gradingsystem.tesla.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradingService {

    private final CohereClientService cohereClientService;
    private final PromptLoader promptLoader;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public String evaluate(String matchedJson) {
        try {
            JsonNode matchedArray = objectMapper.readTree(matchedJson);
            List<ObjectNode> gradedResults = new ArrayList<>();

            log.debug("Grading-Service: Starting evaluation of {} questions", matchedArray.size());

            for (JsonNode q : matchedArray) {
                String question = q.path("question").asText("");
                String answer = q.path("answer").asText("");
                String type = q.path("type").asText("objective");
                int maxScore = q.path("maxScore").asInt(10);
                int rubricWeight = q.has("rubricWeight") ? q.get("rubricWeight").asInt(0) : 0;
                String rubric = q.has("rubricJson") ? q.get("rubricJson").toString() : "{}";
                String questionNumber = q.path("questionNumber").asText("");

                log.debug("Grading-Service: Evaluating question {} - Type: {}, MaxScore: {}, RubricWeight: {}",
                        questionNumber, type, maxScore, rubricWeight);

                String prompt = type.equalsIgnoreCase("subjective")
                        ? buildSubjectivePrompt(question, answer, maxScore, rubric, rubricWeight)
                        : buildObjectivePrompt(question, answer, maxScore, rubric, rubricWeight);

                log.debug("Grading-Service: Prompt for question {}: {}", questionNumber, prompt);

                JsonNode evaluationNode = objectMapper.readTree(cohereClientService.callCohere(prompt));

                log.debug("Grading-Service: AI response for question {}: {}", questionNumber, evaluationNode);

                // JsonNode evaluationNode;
                // try {
                // evaluationNode = objectMapper.readTree(response);
                // log.debug("Grading-Service: Parsed evaluation JSON for question {}",
                // questionNumber);
                // } catch (Exception parseException) {
                // log.warn("Grading-Service: Failed to parse AI response for question {}: {}",
                // questionNumber,
                // parseException.getMessage());
                // ObjectNode errorNode = objectMapper.createObjectNode();
                // errorNode.put("score", 0);
                // errorNode.put("feedback", "Failed to parse AI evaluation response.");
                // evaluationNode = errorNode;
                // }

                ObjectNode graded = objectMapper.createObjectNode();
                graded.put("questionNumber", questionNumber);
                graded.put("question", question);
                graded.put("answer", answer);
                graded.put("maxScore", maxScore);
                graded.set("evaluation", evaluationNode);

                gradedResults.add(graded);
            }

            String resultJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(gradedResults);
            log.debug("Grading-Service: Final graded result JSON: {}", resultJson);
            return resultJson;

        } catch (Exception e) {
            log.error("Grading-Service: Error during evaluation", e);
            throw new RuntimeException("Error evaluating submission: " + e.getMessage(), e);
        }
    }

    private String escapeQuotes(String str) {
        return str == null ? "" : str.replace("\"", "\\\"");
    }

    private String buildSubjectivePrompt(String question, String answer, int maxScore, String rubric, int weight) {
        String promptTemplate = promptLoader.loadPrompt("prompts/subjective_prompt.txt");
        return String.format(promptTemplate, escapeQuotes(question), escapeQuotes(answer), maxScore, rubric, weight);
    }

    private String buildObjectivePrompt(String question, String answer, int maxScore, String rubric, int weight) {
        String promptTemplate = promptLoader.loadPrompt("prompts/objective_prompt.txt");
        return String.format(promptTemplate, escapeQuotes(question), escapeQuotes(answer), maxScore, rubric, weight);
    }
}