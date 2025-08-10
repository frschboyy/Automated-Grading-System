package com.gradingsystem.tesla.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cohere.api.Cohere;
import com.cohere.api.resources.v2.requests.V2ChatRequest;
import com.cohere.api.types.ChatMessageV2;
import com.cohere.api.types.ChatResponse;
import com.cohere.api.types.UserMessage;
import com.cohere.api.types.UserMessageContent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CohereClientService {
    
    @Value("${cohere.api.key}")
    private String apiKey;

    @Value("${cohere.api.model}")
    private String apiModel;

    private Cohere cohere;

    @PostConstruct
    public void init() {
        this.cohere = Cohere.builder().token(apiKey).build();
    }

    public String callCohere(String prompt) {
        V2ChatRequest request = V2ChatRequest.builder()
                .model(apiModel)
                .messages(List.of(ChatMessageV2.user(
                        UserMessage.builder()
                                .content(UserMessageContent.of(prompt))
                                .build())))
                .build();
        ChatResponse response = cohere.v2().chat(request);
        return extractResponse(response.toString());
    }

    private static String extractResponse(String responseBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);

            // Navigate into the Cohere v2 Chat structure
            JsonNode contentArray = root.path("message").path("content");
            if (contentArray.isArray() && contentArray.size() > 0) {
                String innerText = contentArray.get(0).path("text").asText();
                return innerText.trim();
            }

            throw new IllegalArgumentException("No text content found in Cohere response.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract evaluation from response: " + e.getMessage(), e);
        }
    }
}
