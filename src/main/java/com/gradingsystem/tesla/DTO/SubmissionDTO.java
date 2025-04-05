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
public class SubmissionDTO {

    // Default access modifier: package-private
    String studentName;

    // Default access modifier: package-private
    String studentEmail;

    // Default access modifier: package-private
    Long assignmentId;

    // Default access modifier: package-private
    Long studentId;
}
