package com.gradingsystem.tesla.dto;

import java.time.LocalDateTime;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentForm {
    private Long courseId;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private int rubricWeight;
    private MultipartFile assignmentFile;
    private MultipartFile rubricFile;
}
