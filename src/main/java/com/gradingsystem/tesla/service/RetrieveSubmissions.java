package com.gradingsystem.tesla.service;

import com.gradingsystem.tesla.DTO.SubmissionDTO;
import com.gradingsystem.tesla.model.DocumentSubmission;
import com.gradingsystem.tesla.repository.DocumentSubmissionRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RetrieveSubmissions {

    @Autowired
    DocumentSubmissionRepository submissionRepository;

    public List<SubmissionDTO> getSubmissions(Long assignmentId) {
        // Fetch submissions from the repository
        List<DocumentSubmission> submissions = submissionRepository.findByAssignmentId(assignmentId);

        // Map DocumentSubmission entities to SubmissionDTO
        return submissions.stream()
                .map(submission -> SubmissionDTO.builder()
                        .studentName(submission.getStudent().getUsername())
                        .studentEmail(submission.getStudent().getEmail())
                        .assignmentId(submission.getAssignment().getId())
                        .studentId(submission.getStudent().getId())
                        .build())
                .collect(Collectors.toList());
    }
}
