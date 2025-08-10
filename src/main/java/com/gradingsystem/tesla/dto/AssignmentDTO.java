package com.gradingsystem.tesla.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentDTO {
    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private String dueDate;
    private int rubricWeight;
    private String assignmentFileUrl;
}