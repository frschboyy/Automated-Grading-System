package com.gradingsystem.tesla.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gradingsystem.tesla.dto.AssignmentDTO;
import com.gradingsystem.tesla.service.AssignmentService;
import com.gradingsystem.tesla.service.UserService;
import com.gradingsystem.tesla.util.CustomUserDetails;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    private final UserService studentService;
    private final AssignmentService assignmentService;

    @Autowired
    public StudentController(UserService studentService,
            AssignmentService assignmentService) {
        this.studentService = studentService;
        this.assignmentService = assignmentService;
    }

    // Enroll student into a course by course code
    @PostMapping("/courses/enroll")
    public ResponseEntity<?> enrollInCourse(@AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody Map<String, String> body) {
        String courseCode = body.get("courseCode");
        if (courseCode == null || courseCode.isBlank()) {
            logger.warn("Enroll attempt with empty or missing courseCode");
            return ResponseEntity.badRequest().body("Course code is required");
        }

        Long studentId = currentUser.getUser().getId();
        logger.info("Student with ID {} attempting to enroll in course with code '{}'", studentId, courseCode);

        boolean enrolled = studentService.enrollStudentInCourse(studentId, courseCode.trim());

        if (enrolled) {
            logger.info("Student with ID {} successfully enrolled in course '{}'", studentId, courseCode);
            return ResponseEntity.ok("Enrolled successfully");
        } else {
            logger.warn("Enrollment failed for student with ID {} in course '{}': course not found or already enrolled",
                    studentId, courseCode);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course code not found or already enrolled");
        }
    }

    // @PostMapping("/set-course")
    // public ResponseEntity<?> setCourseInSession(HttpSession session, @RequestBody Map<String, Long> body) {
    //     Long courseId = body.get("courseId");
    //     if (courseId == null) {
    //         return ResponseEntity.badRequest().body("courseId is required");
    //     }
    //     session.setAttribute("courseId", courseId);
    //     return ResponseEntity.ok("Course set in session");
    // }

    // // Get upcoming assignments for a course and student
    // @GetMapping("/assignments/upcoming")
    // public ResponseEntity<List<AssignmentDTO>> getUpcomingAssignments(
    // @AuthenticationPrincipal CustomUserDetails currentUser,
    // @RequestParam Long courseId) {
    // Long studentId = currentUser.getUser().getId();
    // logger.info("Fetching upcoming assignments for student ID {} in course ID
    // {}", studentId, courseId);

    // List<AssignmentDTO> assignments =
    // assignmentService.getUpcomingAssignmentsForStudentAndCourse(studentId,
    // courseId);
    // logger.info("Found {} upcoming assignments for student {} in course {}",
    // assignments.size(), studentId,
    // courseId);

    // return ResponseEntity.ok(assignments);
    // }

    // // Get submitted assignments for a course and student
    // @GetMapping("/assignments/submitted")
    // public ResponseEntity<List<AssignmentDTO>> getSubmittedAssignments(
    // @AuthenticationPrincipal CustomUserDetails currentUser,
    // @RequestParam Long courseId) {
    // Long studentId = currentUser.getUser().getId();
    // logger.info("Fetching submitted assignments for student ID {} in course ID
    // {}", studentId, courseId);

    // List<AssignmentDTO> assignments =
    // assignmentService.getSubmittedAssignmentsForStudentAndCourse(studentId,
    // courseId);
    // logger.info("Found {} submitted assignments for student {} in course {}",
    // assignments.size(), studentId,
    // courseId);

    // return ResponseEntity.ok(assignments);
    // }

    // @PostMapping("/set-assignment")
    // public ResponseEntity<?> setAssignmentInSession(HttpSession session, @RequestBody Map<String, Long> body) {
    //     Long assignmentId = body.get("assignmentId");
    //     if (assignmentId == null) {
    //         return ResponseEntity.badRequest().body("assignmentId is required");
    //     }
    //     session.setAttribute("assignmentId", assignmentId);
    //     return ResponseEntity.ok("Assignment set in session");
    // }
}
