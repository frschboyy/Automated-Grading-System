package com.gradingsystem.tesla.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.gradingsystem.tesla.util.LogUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentParserService {

    private final TextExtraction textExtraction;
    private final PromptLoader promptLoader;
    private final CohereClientService cohereClientService;
    private final FallbackServices fallbackServices;

    // Parse assignment 
    public String parseAssignment(MultipartFile assignmentFile) {
        String assignmentText = extractText(assignmentFile);
        String prompt = buildPrompt(assignmentText);

        try {
            return cohereClientService.callCohere(prompt);
        } catch (RuntimeException e) {
            log.warn("AI parsing failed ({}), falling back to local parser", e.getMessage());
            return fallbackServices.parseAssignment(assignmentText);
        }
    }

    // Extract assignment file submitted by teacher
    private String extractText(MultipartFile assignmentFile) {
        try {
            log.debug("Starting assignment parsing...");
            String assignmentText = textExtraction.extractText(assignmentFile);
            log.debug("Extracted assignment text: {}", LogUtils.truncate(assignmentText, 2000));
            return assignmentText;
        } catch (IOException e) {
            log.error("Error extracting text from assignment file", e);
            throw new RuntimeException("Assignment text extraction failed", e);
        } catch (Exception e) {
            log.error("Unexpected error during assignment parsing", e);
            throw new RuntimeException("Unexpected error during assignment parsing", e);
        }
    }

    // Build prompt from assignment text
    private String buildPrompt(String assignmentText) {
        String promptTemplate = promptLoader.loadPrompt("prompts/assignment_parser_prompt.txt");
        return String.format(promptTemplate, assignmentText);
    }
}
