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
    private Long submissionId;
    private Long studentId;
    private String StudentRegistrationId;
    private String studentName;
    private String dateSubmitted;
}