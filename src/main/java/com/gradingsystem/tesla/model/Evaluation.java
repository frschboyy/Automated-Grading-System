package com.gradingsystem.tesla.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "evaluation")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Evaluation {
    @Id
    private Long id;

    @OneToOne
    @JoinColumn(name = "submission_id", referencedColumnName = "id")
    private DocumentSubmission submission;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String evaluationJson;
}
