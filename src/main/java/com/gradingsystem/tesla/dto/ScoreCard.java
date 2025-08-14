package com.gradingsystem.tesla.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScoreCard {
    private Double score;
    private Double maxScore;
    private Double percentage;
}