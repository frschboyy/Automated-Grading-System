package com.gradingsystem.tesla.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "assignments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime dueDate;

    // Firebase file path or public URL    
    private String rubricFileUrl;
    private String assignmentFileUrl;

    private int rubricWeight;

    // Parsed Rubric file stored in JSON format
    @Column(columnDefinition = "TEXT")
    private String parsedRubricJson;

    // Parsed Assignment file stored in JSON format
    @Column(columnDefinition = "TEXT")
    private String parsedAssignmentQuestions;

    // Audit Fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Associated course
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentSubmission> submissions;
}
