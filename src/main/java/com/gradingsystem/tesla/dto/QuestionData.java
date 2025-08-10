package com.gradingsystem.tesla.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionData {
    private String questionNumber;
    private String question;
    private String type;
    private String expectedAnswer;
    private String answer;
    private Integer maxScore;
    private String rubricJson;
    private Integer rubricWeight;
}