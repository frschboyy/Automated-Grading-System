package com.gradingsystem.tesla.service;

import com.gradingsystem.tesla.util.LogUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class RubricParserService {
    
    private final CohereClientService cohereClientService;
    private final TextExtraction textExtraction;
    private final PromptLoader promptLoader;
    private final FallbackServices fallbackServices;

    public String parseRubric(MultipartFile rubricFile) {
        String rubricText;
        try {
            rubricText = textExtraction.extractText(rubricFile);
            log.debug("Extracted rubric text: {}", LogUtils.truncate(rubricText, 2000));
        } catch (Exception e) {
            log.error("Error extracting text from rubric file", e);
            // Extraction failed - fallback with empty input
            return "";
        }

        String promptTemplate = promptLoader.loadPrompt("prompts/rubric_parser_prompt.txt");
        String prompt = String.format(promptTemplate, rubricText);

        try {
            return cohereClientService.callCohere(prompt);
        } catch (Exception e) {
            log.warn("Cohere API failed. Falling back to basic rubric parser.", e);
            return fallbackServices.parseRubric(rubricText);
        }
    }
}
