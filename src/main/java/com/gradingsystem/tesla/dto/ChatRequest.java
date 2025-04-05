package com.gradingsystem.tesla.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ChatRequest {

    private String model;
    private List<Message> messages;

    // Use final for parameters as they are not reassigned within the constructor
    public ChatRequest(final String model, final String prompt) {
        this.model = model;
        this.messages = new ArrayList<>();
        this.messages.add(new Message("user", prompt));
    }
}