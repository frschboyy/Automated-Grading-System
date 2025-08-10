package com.gradingsystem.tesla.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CombinedQA {
    private String questionNumber;
    private String question;
    private String type;
    private String answer;
    private int maxScore;
}
