package com.gradingsystem.tesla.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gradingsystem.tesla.dto.StudentDTO;
import com.gradingsystem.tesla.dto.SubmissionDTO;
import com.gradingsystem.tesla.model.Assignment;
import com.gradingsystem.tesla.model.DocumentSubmission;
import com.gradingsystem.tesla.model.Evaluation;
import com.gradingsystem.tesla.model.User;
import com.gradingsystem.tesla.repository.DocumentSubmissionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final DocumentSubmissionRepository submissionRepository;
    private final AssignmentService assignmentService;
    private final UserService studentService;

    public DocumentSubmission saveSubmission(Long assignmentId, Long studentId, String fileUrl, String parsedJson) {
        User student = studentService.getUser(studentId);
        Assignment assignment = assignmentService.getAssignment(assignmentId);
        DocumentSubmission submission = DocumentSubmission.builder()
                .assignment(assignment)
                .student(student)
                .fileUrl(fileUrl)
                .parsedJson(parsedJson)
                .submittedAt(LocalDateTime.now())
                .build();

        return submissionRepository.save(submission);
    }

    public void updateEvaluation(Long submissionId, String evaluationJson) {
        try {
            DocumentSubmission submission = submissionRepository.findById(submissionId)
                    .orElseThrow(() -> new RuntimeException("Submission not found"));

            // Create Evaluation object
            Evaluation evaluation = Evaluation.builder()
                    .id(submissionId)
                    .evaluationJson(evaluationJson)
                    .submission(submission)
                    .build();

            // Link back to submission
            submission.setEvaluation(evaluation);

            // Save
            submissionRepository.save(submission);

        } catch (Exception e) {
            throw new RuntimeException("Failed to save evaluation", e);
        }
    }

    public List<SubmissionDTO> getAllSubmissions(Long assignmentId, Long courseId) {
        List<SubmissionDTO> submissionList = new ArrayList<SubmissionDTO>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy");
        List<User> students = studentService.getStudentsForCourse(courseId);
        for (User student : students) {
            DocumentSubmission submission = submissionRepository.findByStudent(student);
            if (submission != null) {
                SubmissionDTO dto = new SubmissionDTO();
                dto.setSubmissionId(submission.getId());
                dto.setStudentId(student.getId());
                dto.setStudentRegistrationId(student.getRegistrationId());
                dto.setStudentName(
                        student.getFirstName() + " " + student.getMiddleName() + " " + student.getLastName());
                dto.setDateSubmitted(submission.getSubmittedAt().format(formatter));
                submissionList.add(dto);
            }
        }
        return submissionList;
    }

    public List<StudentDTO> getAllPendingSubmissions(Long assignmentId, Long courseId) {
        List<StudentDTO> pendingList = new ArrayList<StudentDTO>();
        List<User> students = studentService.getStudentsForCourse(courseId);
        for (User student : students) {
            DocumentSubmission submission = submissionRepository.findByStudent(student);
            if (submission == null) {
                StudentDTO dto = new StudentDTO();
                dto.setRegistrationId(student.getRegistrationId());
                dto.setName(student.getFirstName() + " " + student.getMiddleName() + " " + student.getLastName());
                dto.setEmail(student.getEmail());
                pendingList.add(dto);
            }
        }
        return pendingList;
    }
}
