package com.gradingsystem.tesla.service;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;

@Service
public class PromptLoader {

    public String loadPrompt(String fileName) {
        try {
            return new String(
                Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(fileName).toURI()))
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to load prompt file: " + fileName, e);
        }
    }
}
