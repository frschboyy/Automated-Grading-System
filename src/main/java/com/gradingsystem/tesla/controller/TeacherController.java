package com.gradingsystem.tesla.controller;

import com.gradingsystem.tesla.dto.AssignmentDTO;
import com.gradingsystem.tesla.dto.AssignmentForm;
import com.gradingsystem.tesla.dto.CourseDTO;
import com.gradingsystem.tesla.dto.EvaluationUpdateRequest;
import com.gradingsystem.tesla.dto.StudentDTO;
import com.gradingsystem.tesla.util.CustomUserDetails;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import com.gradingsystem.tesla.service.AssignmentService;
import com.gradingsystem.tesla.service.UserService;
import com.gradingsystem.tesla.service.CourseService;
import com.gradingsystem.tesla.service.SubmissionService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/teacher")
@PreAuthorize("hasRole('TEACHER')")
@RequiredArgsConstructor
public class TeacherController {

    private final CourseService courseService;
    private final AssignmentService assignmentService;
    private final UserService userService;
    private final SubmissionService submissionService;

    // COURSES
    @GetMapping("/courses")
    public ResponseEntity<List<CourseDTO>> getTeacherCourses(@AuthenticationPrincipal CustomUserDetails currentUser) {
        Long teacherId = currentUser.getUser().getId();
        List<CourseDTO> courses = courseService.getCoursesForTeacher(teacherId)
                .stream()
                .map(c -> new CourseDTO(c.getId(), c.getName(), c.getCourseCode()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(courses);
    }

    // ASSIGNMENTS
    @GetMapping("/course/{courseId}/assignments")
    public ResponseEntity<List<AssignmentDTO>> getAssignmentsByCourse(@PathVariable Long courseId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpSession session) {
        Long teacherId = currentUser.getUser().getId();
        session.setAttribute("courseId", courseId);
        List<AssignmentDTO> assignments = assignmentService.getAssignmentsForTeacherCourse(teacherId, courseId);

        return ResponseEntity.ok(assignments);
    }

    @PostMapping("/assignments/create")
    public ResponseEntity<String> createAssignment(@AuthenticationPrincipal CustomUserDetails currentUser,
            @ModelAttribute AssignmentForm dto) throws IOException {

        Long teacherId = currentUser.getUser().getId();
        assignmentService.createAssignmentForCourse(dto, teacherId);

        return ResponseEntity.ok("Assignment created successfully");
    }

    @GetMapping("/assignments/{id}")
    public ResponseEntity<AssignmentDTO> getAssignment(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.getAssignmentById(id));
    }

    @PutMapping(value = "/assignments/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateAssignment(@PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("dueDate") String dueDate,
            @RequestParam("rubricWeight") int rubricWeight,
            @RequestParam(value = "rubricDocument", required = false) MultipartFile rubricFile,
            @RequestParam(value = "assignmentDocument", required = false) MultipartFile assignmentFile)
            throws Exception {
        assignmentService.updateAssignment(id, title, description, dueDate, rubricWeight, rubricFile, assignmentFile);
        return ResponseEntity.ok("Assignment updated successfully");
    }

    @DeleteMapping("/assignments/{id}")
    public ResponseEntity<String> deleteAssignment(@PathVariable Long id) {
        assignmentService.deleteAssignmentById(id);
        return ResponseEntity.ok("Assignment deleted successfully");
    }

    // Query all students for a course
    @GetMapping("/students/course/{courseId}")
    public ResponseEntity<List<StudentDTO>> getStudents(@PathVariable Long courseId) {
        List<StudentDTO> students = userService.getStudentsForCourse(courseId)
                .stream()
                .map(s -> new StudentDTO(s.getId(), s.getRegistrationId(),
                        s.getFirstName() + " " + s.getMiddleName() + " " + s.getLastName(),
                        s.getEmail()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(students);
    }

    // Update evaluation for a question
    @PostMapping("/evaluation/update/{questionNumber}")
    public ResponseEntity<?> updateEvaluation(
            @PathVariable int questionNumber,
            @RequestBody EvaluationUpdateRequest updateRequest,
            HttpSession session) {
        
        Long submissionId = (Long) session.getAttribute("submissionId");
        
        try {
            boolean updated = submissionService.updateEvaluation(submissionId, questionNumber, updateRequest);
            if (updated) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Evaluation not found");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating evaluation");
        }
    }
}
