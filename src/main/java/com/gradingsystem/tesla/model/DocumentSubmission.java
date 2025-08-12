package com.gradingsystem.tesla.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne()
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    private String fileUrl;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String parsedJson;
    
    @Column(nullable = true)
    private Integer grade;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime submittedAt;
}