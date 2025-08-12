package com.gradingsystem.tesla.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gradingsystem.tesla.model.Evaluation;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    List<Evaluation> findBySubmissionId(Long submissionId);

    Optional<Evaluation> findBySubmissionIdAndQuestionNumber(Long submissionId, int questionNumber);
}

