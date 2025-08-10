package com.gradingsystem.tesla.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class FallbackServices {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Simple fallback parser that assumes each line is one question, no numbering
    public String parseAssignment(String assignmentText) {
        log.warn("Using fallback parser (non-AI), no numbering assumed");

        String[] lines = assignmentText.split("\\r?\\n+");
        List<Map<String, Object>> questions = new ArrayList<>();
        int counter = 1;

        for (String line : lines) {
            String question = line.trim();
            if (question.isEmpty())
                continue;
            if (question.length() < 15) {
                log.debug("Skipping short line: {}", question);
                continue;
            }

            Map<String, Object> q = new LinkedHashMap<>();
            q.put("questionNumber", String.valueOf(counter++));
            q.put("question", question);
            q.put("type", classifyQuestion(question));
            q.put("maxScore", 5);

            log.debug("Parsed question #{}: {}", counter - 1, question);
            questions.add(q);
        }

        try {
            String json = new ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(questions);
            log.debug("Fallback parser JSON output:\n{}", json);
            return json;
        } catch (Exception e) {
            log.error("Fallback parser JSON serialization failed", e);
            throw new RuntimeException("Fallback parsing failed during JSON serialization", e);
        }
    }

    // Basic classification (objective/subjective)
    private String classifyQuestion(String question) {
        if (question == null || question.trim().isEmpty()) {
            return "subjective"; // Default
        }

        String lower = question.toLowerCase();

        String[] objectiveKeywords = {
                "define", "list", "state", "what is", "which of the following", "choose", "name",
                "identify", "give", "mention", "tick", "select", "indicate", "true or false", "fill in the blanks",
                "multiple choice", "match the following", "enumerate"
        };

        for (String keyword : objectiveKeywords) {
            if (lower.contains(keyword)) {
                return "objective";
            }
        }

        if (lower.matches(".*\\b(who|when|where|how many|how much|what year|in which)\\b.*")) {
            return "objective";
        }

        String[] subjectiveKeywords = {
                "explain", "discuss", "describe", "analyze", "evaluate", "justify", "illustrate",
                "critique", "compare", "contrast", "argue", "interpret", "summarize", "assess", "elaborate"
        };

        for (String keyword : subjectiveKeywords) {
            if (lower.contains(keyword)) {
                return "subjective";
            }
        }

        if (lower.length() <= 40 && lower.endsWith("?")) {
            return "objective";
        }

        return "subjective";
    }

    /**
     * Fallback rubric format expected (non-AI):
     * 
     * Criterion: Clarity
     * Description: The answer is clear and easy to understand.
     * Weight: 30
     *
     * Criterion: Completeness
     * Description: The answer addresses all parts of the question.
     * Weight: 40
     *
     * Criterion: Accuracy
     * Description: Facts and explanations provided are correct.
     * Weight: 30
     */
    public String parseRubric(String rubricText) {
        log.warn("Using fallback rubric parser (non-AI)");

        List<Map<String, Object>> criteriaList = new ArrayList<>();
        Pattern pattern = Pattern.compile(
                "(?i)Criterion\\s*:\\s*(.*?)\\s*\\n" +
                        "Description\\s*:\\s*(.*?)\\s*\\n" +
                        "Weight\\s*:\\s*(\\d+)",
                Pattern.DOTALL);

        Matcher matcher = pattern.matcher(rubricText);

        while (matcher.find()) {
            String name = matcher.group(1).trim();
            String description = matcher.group(2).trim();
            int weight = Integer.parseInt(matcher.group(3).trim());

            Map<String, Object> criterion = new LinkedHashMap<>();
            criterion.put("name", name);
            criterion.put("description", description);
            criterion.put("weight", weight);

            criteriaList.add(criterion);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("criteria", criteriaList);

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
        } catch (Exception e) {
            throw new RuntimeException("Fallback rubric parsing failed during JSON serialization", e);
        }
    }

    /**
     * Fallback parser using regex to extract question number, question, and answer
     * from the raw submission text in case Cohere API fails.
     * Limitation: Requires numbering
     */
    public String parseSubmission(String submissionText) throws Exception {
        log.warn("Using fallback parser for submission text.");

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode resultArray = mapper.createArrayNode();

        // Regex pattern to match questions numbered like 1., 2a., 3b), etc.
        Pattern questionPattern = Pattern.compile(
                "(?m)^\\s*(\\d+[a-zA-Z]?)(?:\\.|\\))\\s*(.+?)(?=\\n\\d+[a-zA-Z]?(?:\\.|\\))|\\z)",
                Pattern.DOTALL);

        Matcher matcher = questionPattern.matcher(submissionText);

        while (matcher.find()) {
            String questionNumber = matcher.group(1).trim();
            String fullText = matcher.group(2).trim();

            // Split first line as question, rest as answer
            String[] parts = fullText.split("\\r?\\n", 2);
            String question = parts[0].trim();
            String answer = parts.length > 1 ? parts[1].trim() : "";

            // Clean answer from separator lines like "______"
            answer = answer.replaceAll("[-_]{3,}", "").trim();

            ObjectNode qna = mapper.createObjectNode();
            qna.put("questionNumber", questionNumber);
            qna.put("question", question);
            qna.put("answer", answer);

            resultArray.add(qna);
        }

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultArray);
    }
}
