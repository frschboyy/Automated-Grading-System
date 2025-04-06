package com.gradingsystem.tesla.model;

import jakarta.persistence.*;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "document_submission")
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
    @JoinColumn(name = "student_id") // Foreign Key Column
    private Student student;

    @ManyToOne()
    @JoinColumn(name = "assignment_id", nullable = false, foreignKey = @ForeignKey(name = "FKkc7avf6jcqtj1qodcb3n9ijr3")) // Foreign Key Column
    private Assignment assignment; // Foreign Key Column


    @Lob
    @Column(nullable = false, columnDefinition = "LONGBLOB")
    private byte[] extractedText;

    @Column(nullable = false, length = 64) // SHA-256 produces 64-character hex strings
    private String hashValue;

    @Column(nullable = true)
    private Integer grade;

    @Column(nullable = true)
    private Integer similarityScore;

    @ElementCollection
    @CollectionTable(name = "evaluation_results", joinColumns = @JoinColumn(name = "submission_id"))
    @MapKeyColumn(name = "question")
    @Column(name = "score")
    private Map<String, String> evaluationResults;
}