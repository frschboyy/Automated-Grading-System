package com.gradingsystem.tesla.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EvaluationDTO {
    private Long id;
    private int questionNumber;
    private String question;
    private String answer;
    private String score;
    private String maxScore;
    private String feedback;
}