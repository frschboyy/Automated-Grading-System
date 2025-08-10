package com.gradingsystem.tesla.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MatchingService {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // Matches teacher assignment questions with student submission answers.
    public String matchQuestions(String teacherAssignmentJson, String studentSubmissionJson) {
        try {
            JsonNode teacherArray = objectMapper.readTree(teacherAssignmentJson);
            JsonNode studentArray = objectMapper.readTree(studentSubmissionJson);

            List<JsonNode> studentNodes = new ArrayList<>();
            studentArray.forEach(studentNodes::add);

            List<JsonNode> combinedList = new ArrayList<>();

            for (JsonNode teacherQuestion : teacherArray) {
                String teacherNumber = teacherQuestion.get("questionNumber").asText();

                // Direct match based on questionNumber
                JsonNode matchedStudent = studentNodes.stream()
                        .filter(stu -> stu.has("questionNumber") &&
                                stu.get("questionNumber").asText().equalsIgnoreCase(teacherNumber))
                        .findFirst()
                        .orElse(null);

                String studentAnswer = (matchedStudent != null && matchedStudent.has("answer"))
                        ? matchedStudent.get("answer").asText() : "";

                // Build combined object
                var node = objectMapper.createObjectNode();
                node.put("questionNumber", teacherNumber);
                node.put("question", teacherQuestion.get("question").asText());
                node.put("type", teacherQuestion.get("type").asText());
                node.put("answer", studentAnswer);
                node.put("maxScore", teacherQuestion.get("maxScore").asInt());

                combinedList.add(node);
            }

            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(combinedList);

        } catch (Exception e) {
            log.error("Error matching questions", e);
            throw new RuntimeException("Error matching questions", e);
        }
    }
}