package com.gradingsystem.tesla.service;

// import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import com.gradingsystem.tesla.dto.EvaluationDTO;
import com.gradingsystem.tesla.model.Evaluation;
import com.gradingsystem.tesla.model.DocumentSubmission;
import com.gradingsystem.tesla.util.CustomUserDetails;
import com.gradingsystem.tesla.repository.EvaluationRepository;
import com.gradingsystem.tesla.repository.DocumentSubmissionRepository;
import org.springframework.security.access.AccessDeniedException;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RetrieveEvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final DocumentSubmissionRepository submissionRepository;

    public List<EvaluationDTO> getEvaluationData(Long submissionId, CustomUserDetails currentUser) {
        DocumentSubmission submission = submissionRepository.findById(submissionId)
            .orElseThrow(() -> new RuntimeException("submission not found"));

        List<Evaluation> evaluations = evaluationRepository.findBySubmissionId(submissionId);

        if(!(submission.getStudent().getId().equals(currentUser.getUser().getId()) ||
            submission.getAssignment().getCourse().getTeacher().getId().equals(currentUser.getUser().getId()))){
                throw new AccessDeniedException("No access to these details");
        }

        return evaluations.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private EvaluationDTO toDTO(Evaluation e) {
        return EvaluationDTO.builder()
                .id(e.getId())
                .questionNumber(e.getQuestionNumber())
                .question(e.getQuestion())
                .answer(e.getAnswer())
                .maxScore(e.getMaxScore())
                .score(e.getScore())
                .feedback(e.getFeedback())
                .build();
    }
}
