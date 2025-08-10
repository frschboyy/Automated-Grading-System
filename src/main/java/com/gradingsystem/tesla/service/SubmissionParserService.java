package com.gradingsystem.tesla.service;

import com.gradingsystem.tesla.util.LogUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionParserService {

    private final CohereClientService cohereClientService;
    private final TextExtraction textExtraction;
    private final PromptLoader promptLoader;
    private final FallbackServices fallbackServices;

    public String parseSubmission(MultipartFile file) throws Exception {
        String submissionText;
        try {
            submissionText = textExtraction.extractText(file);
            log.debug("Extracted submission text: {}", LogUtils.truncate(submissionText, 2000));
        } catch (IOException e) {
            log.error("Error extracting text from submission file", e);
            throw new RuntimeException("Submission text extraction failed", e);
        }

        String promptTemplate = promptLoader.loadPrompt("prompts/submission_parser_prompt.txt");
        String prompt = String.format(promptTemplate, submissionText);

        try {
            return cohereClientService.callCohere(prompt);
        } catch (Exception e) {
            log.error("Error calling Cohere API for submission parsing", e);
            return fallbackServices.parseSubmission(submissionText);
        }
    }
}
