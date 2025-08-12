package com.gradingsystem.tesla.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gradingsystem.tesla.service.UserService;
import com.gradingsystem.tesla.util.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final UserService studentService;

    // Enroll student into a course by course code
    @PostMapping("/courses/enroll")
    public ResponseEntity<?> enrollInCourse(@AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody Map<String, String> body) {
        String courseCode = body.get("courseCode");
        if (courseCode == null || courseCode.isBlank()) {
            log.warn("Enroll attempt with empty or missing courseCode");
            return ResponseEntity.badRequest().body("Course code is required");
        }

        Long studentId = currentUser.getUser().getId();
        log.info("Student with ID {} attempting to enroll in course with code '{}'", studentId, courseCode);

        boolean enrolled = studentService.enrollStudentInCourse(studentId, courseCode.trim());

        if (enrolled) {
            log.info("Student with ID {} successfully enrolled in course '{}'", studentId, courseCode);
            return ResponseEntity.ok("Enrolled successfully");
        } else {
            log.warn("Enrollment failed for student with ID {} in course '{}': course not found or already enrolled",
                    studentId, courseCode);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course code not found or already enrolled");
        }
    }
}
