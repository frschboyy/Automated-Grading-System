package com.gradingsystem.tesla.service;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.gradingsystem.tesla.dto.AssignmentDTO;
import com.gradingsystem.tesla.dto.AssignmentForm;
import com.gradingsystem.tesla.model.Assignment;
import com.gradingsystem.tesla.model.Course;
import com.gradingsystem.tesla.model.DocumentSubmission;
import com.gradingsystem.tesla.repository.AssignmentRepository;
import com.gradingsystem.tesla.repository.CourseRepository;
import com.gradingsystem.tesla.repository.DocumentSubmissionRepository;
import com.gradingsystem.tesla.util.CustomUserDetails;
import com.gradingsystem.tesla.util.PathUtils;

import org.springframework.security.access.AccessDeniedException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final CourseRepository courseRepository;
    private final AssignmentRepository assignmentRepository;
    private final DocumentSubmissionRepository documentSubmissionRepository;
    private final FirebaseStorageService firebaseStorageService;
    private final RubricParserService rubricParserService;
    private final AssignmentParserService assignmentParserService;

    // Create a new assignment
    @Transactional
    public void createAssignmentForCourse(AssignmentForm dto, Long teacherId) throws IOException {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        // Save assignment
        Assignment assignment = Assignment.builder()
                .course(course)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .dueDate(dto.getDueDate())
                .rubricWeight(dto.getRubricWeight())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        assignment = assignmentRepository.save(assignment);

        // Build common path prefix
        String pathPrefix = PathUtils.sanitizePathPart(course.getCourseCode()) + "/"
                + assignment.getId() + "_" + PathUtils.sanitizePathPart(assignment.getTitle());

        // Upload assignment file
        MultipartFile assignmentFile = dto.getAssignmentFile();
        if (assignmentFile != null && !assignmentFile.isEmpty()) {
            String firebasePath = pathPrefix + "/assignment_"
                    + PathUtils.sanitizePathPart(assignmentFile.getOriginalFilename());
            try {
                firebaseStorageService.uploadFile(
                        assignmentFile.getBytes(),
                        firebasePath,
                        assignmentFile.getContentType());

                assignment.setAssignmentFileUrl(firebasePath);

                // Get and save parsed assignment questions
                assignment.setParsedAssignmentQuestions(
                        assignmentParserService.parseAssignment(assignmentFile));
            } catch (Exception e) {
                throw new IOException("Failed to upload assignment file to Firebase", e);
            }
        }

        // Upload rubric file (optional)
        MultipartFile rubricFile = dto.getRubricFile();
        if (rubricFile != null && !rubricFile.isEmpty()) {
            String firebasePath = pathPrefix + "/rubric_"
                    + PathUtils.sanitizePathPart(rubricFile.getOriginalFilename());
            try {
                firebaseStorageService.uploadFile(
                        rubricFile.getBytes(),
                        firebasePath,
                        rubricFile.getContentType());
                assignment.setRubricFileUrl(firebasePath);

                // Parse Rubric to JSON w/ AI
                assignment.setParsedRubricJson(rubricParserService.parseRubric(rubricFile));
            } catch (Exception e) {
                throw new IOException("Failed to upload rubric file to Firebase", e);
            }
        } else {
            assignment.setRubricWeight(0);
        }

        assignmentRepository.save(assignment);
    }

    // Update assignment, upload rubric file to Firebase Storage and save path
    public void updateAssignment(Long id, String title, String description, String dueDate,
            int rubricWeight, MultipartFile rubricFile, MultipartFile assignmentFile,
            CustomUserDetails currentUser) throws IOException {

        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (assignment.getCourse().getTeacher().getId().equals(currentUser.getUser().getId())) {
            throw new AccessDeniedException("Not your assignment");
        }

        assignment.setTitle(title);
        assignment.setDescription(description);
        assignment.setDueDate(LocalDateTime.parse(dueDate));
        assignment.setRubricWeight(rubricWeight);

        String pathPrefix = PathUtils.sanitizePathPart(assignment.getCourse().getCourseCode()) + "/"
                + assignment.getId() + "_" + PathUtils.sanitizePathPart(assignment.getTitle());

        // Replace rubric file if provided
        if (rubricFile != null && !rubricFile.isEmpty()) {
            if (assignment.getRubricFileUrl() != null) {
                firebaseStorageService.deleteFile(assignment.getRubricFileUrl()); // delete old file
            }
            String firebasePath = pathPrefix + "/rubric_"
                    + PathUtils.sanitizePathPart(rubricFile.getOriginalFilename());
            try {
                firebaseStorageService.uploadFile(
                        rubricFile.getBytes(),
                        firebasePath,
                        rubricFile.getContentType());
                assignment.setRubricFileUrl(firebasePath);

                // Parse Rubric to JSON w/ AI
                assignment.setParsedRubricJson(rubricParserService.parseRubric(rubricFile));
            } catch (Exception e) {
                throw new IOException("Failed to upload rubric file to Firebase", e);
            }
        }

        // Replace assignment file if provided
        if (assignmentFile != null && !assignmentFile.isEmpty()) {
            if (assignment.getAssignmentFileUrl() != null) {
                firebaseStorageService.deleteFile(assignment.getAssignmentFileUrl()); // delete old file
            }
            String firebasePath = pathPrefix + "/assignment_"
                    + PathUtils.sanitizePathPart(assignmentFile.getOriginalFilename());
            try {
                firebaseStorageService.uploadFile(
                        assignmentFile.getBytes(),
                        firebasePath,
                        assignmentFile.getContentType());
                assignment.setAssignmentFileUrl(firebasePath);

                // Get and save parsed assignment questions
                assignment.setParsedAssignmentQuestions(
                        assignmentParserService.parseAssignment(assignmentFile));
            } catch (Exception e) {
                throw new IOException("Failed to upload assugnment file to Firebase", e);
            }
        }

        assignment.setUpdatedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);
    }

    // Delete an assignment
    public void deleteAssignment(Long assignmentId, CustomUserDetails currentUser) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (assignment.getCourse().getTeacher().getId().equals(currentUser.getUser().getId())) {
            throw new AccessDeniedException("Not your assignment");
        }
        assignmentRepository.deleteById(assignmentId);
    }

    // Get an assignment
    public AssignmentDTO getAssignmentById(Long id) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy");
        Assignment a = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        return AssignmentDTO.builder()
                .id(a.getId())
                .courseId(a.getCourse().getId())
                .title(a.getTitle())
                .description(a.getDescription())
                .dueDate(a.getDueDate().format(formatter))
                .rubricWeight(a.getRubricWeight())
                .build();
    }

    public Assignment getAssignment(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
    }

    // Get upcoming assignments for a student in a course
    public List<AssignmentDTO> getUpcomingAssignmentsForStudentAndCourse(Long studentId, Long courseId) {
        List<Assignment> assignments = assignmentRepository.findByCourseId(courseId);
        List<Long> submittedAssignmentIds = documentSubmissionRepository.findByStudentId(studentId).stream()
                .map(sub -> sub.getAssignment().getId())
                .collect(Collectors.toList());
        List<AssignmentDTO> upcoming = assignments.stream()
                .filter(a -> !submittedAssignmentIds.contains(a.getId()))
                .map(this::toDTO)
                .collect(Collectors.toList());

        return upcoming;
    }

    // Get submitted assignments for a student in a course
    public List<AssignmentDTO> getSubmittedAssignmentsForStudentAndCourse(Long studentId, Long courseId) {
        // Find submissions for student in the course's assignments
        List<DocumentSubmission> submissions = documentSubmissionRepository.findByStudentId(studentId);

        return submissions.stream()
                .filter(sub -> sub.getAssignment().getCourse().getId().equals(courseId))
                .map(sub -> toDTO(sub.getAssignment()))
                .collect(Collectors.toList());
    }

    private AssignmentDTO toDTO(Assignment a) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy");
        return AssignmentDTO.builder()
                .id(a.getId())
                .courseId(a.getCourse().getId())
                .title(a.getTitle())
                .description(a.getDescription())
                .dueDate(a.getDueDate().format(formatter))
                .assignmentFileUrl(a.getAssignmentFileUrl())
                .build();
    }

    // Get all Assignments for a course
    public List<AssignmentDTO> getAssignmentsForTeacherCourse(Long teacherId, Long courseId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy");

        return assignmentRepository.findByTeacherAndCourse(teacherId, courseId).stream()
                .map(a -> AssignmentDTO.builder()
                .id(a.getId())
                .courseId(courseId)
                .title(a.getTitle())
                .description(a.getDescription())
                .dueDate(a.getDueDate().format(formatter))
                .assignmentFileUrl(a.getAssignmentFileUrl())
                .build())
                .collect(Collectors.toList());
    }

    // Get all Assignments
    public List<Assignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    @Transactional
    public boolean deleteAssignmentById(Long id, CustomUserDetails currentUser) {
        Assignment assignment = assignmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (assignment.getCourse().getTeacher().getId().equals(currentUser.getUser().getId())) {
            throw new AccessDeniedException("Not your assignment");
        }
        
        firebaseStorageService.deleteFile(assignment.getAssignmentFileUrl());
        documentSubmissionRepository.deleteAllByAssignment(assignment);
        assignmentRepository.delete(assignment);
        return true;
    }

    public URL getAssignmentDownloadUrl(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        return firebaseStorageService.getFileUrl(assignment.getAssignmentFileUrl(), 1);
    }

    public String getAssignmentJson(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .map(Assignment::getParsedAssignmentQuestions)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
    }
}
