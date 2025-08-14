package com.gradingsystem.tesla.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationUpdateRequest {
    private Double score;
    private Double maxScore;
    private String feedback;
    private Double oldScore;
    private Double oldMaxScore;
}
