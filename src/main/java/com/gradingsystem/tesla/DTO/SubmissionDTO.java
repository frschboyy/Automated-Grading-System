package com.gradingsystem.tesla.DTO;

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
    String studentName;
    String studentEmail;
    Long assignmentId;
    Long studentId;
}

