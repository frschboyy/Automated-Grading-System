package com.gradingsystem.tesla.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvaluationResult {
    private double totalScore;
    private List<QuestionScore> details;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuestionScore {
        private String questionNumber;
        private double score;
        private String feedback;
    }
}
