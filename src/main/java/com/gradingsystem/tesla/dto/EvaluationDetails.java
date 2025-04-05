package com.gradingsystem.tesla.dto;

import java.util.Map;
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
public class EvaluationDetails {

    // Default access modifier: package-private
    int grade;

    // Default access modifier: package-private
    int plagiarismScore;

    // Default access modifier: package-private
    Map<String, String> results;
}
