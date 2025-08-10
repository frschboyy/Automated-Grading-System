package com.gradingsystem.tesla.service;

import com.gradingsystem.tesla.model.DocumentSubmission;
import com.gradingsystem.tesla.repository.DocumentSubmissionRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManageEvaluationService {
    
    private final DocumentSubmissionRepository submissionRepository;

    public List<DocumentSubmission> getEvaluationsForAssignments(Long assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId);
    }

    public void updateEvaluation(Long submissionId, Double grade, String feedback) {
        DocumentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
        submission.setGrade(grade.intValue());
        // submission.s.setFeedback(feedback);
        submissionRepository.save(submission);
    }
}
