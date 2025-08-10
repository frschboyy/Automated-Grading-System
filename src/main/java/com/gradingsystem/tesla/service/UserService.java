package com.gradingsystem.tesla.service;

import com.gradingsystem.tesla.dto.CourseDTO;
import com.gradingsystem.tesla.model.Course;
import com.gradingsystem.tesla.model.Institution;
import com.gradingsystem.tesla.model.User;
import com.gradingsystem.tesla.repository.CourseRepository;
import com.gradingsystem.tesla.repository.UserRepository;
import com.gradingsystem.tesla.validation.UserRoleValidator;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleValidator userRoleValidator;
    private final CourseRepository courseRepository;

    private PasswordEncoder passwordEncoder;

    public User saveUser(User user) {

        // Hash Password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Validate role-based fields before saving
        userRoleValidator.validate(user);

        return userRepository.save(user);
    }

    public User findById(Long id) {
        return userRepository.findById(id).get();
    }

    public User getUser(String email) {
        return userRepository.findByEmail(email);
    }

    public User getUser(Long id) {
        return userRepository.findById(id).get();
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public List<User> findByInstitutionAndRole(Institution institution, String role) {
        return userRepository.findByInstitutionAndRole(institution, role);
    }

    public List<User> findPendingStudents(Institution institution) {
        return userRepository.findByInstitutionAndRoleAndApprovedByAdminFalse(institution, "STUDENT");
    }

    public List<User> findUsersForInstitution(Institution institution, String role) {
        return userRepository.findByInstitutionAndRole(institution, role.toUpperCase());
    }

    public User findByRegistrationId(String registrationId) {
        return userRepository.findByRegistrationId(registrationId);
    }

    public void removeUser(User user) {
        userRepository.delete(user);
    }

    public User findByRegistrationIdAndInstitution(String registrationId, Institution institution) {
        return userRepository.findByRegistrationIdAndInstitution(registrationId, institution);
    }

    public User findByIdAndInstitution(Long id, Institution institution) {
        return userRepository.findByIdAndInstitution(id, institution);
    }

    public List<User> getStudentsForCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return new ArrayList<>(course.getStudents());
    }

    // Get all courses student is enrolled in
    public List<CourseDTO> getCoursesForStudent(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Set<Course> courses = student.getEnrolledCourses();

        return courses.stream()
                .map(c -> new CourseDTO(c.getId(), c.getName(), c.getCourseCode()))
                .sorted(Comparator.comparing(CourseDTO::getCourseCode))
                .collect(Collectors.toList());
    }

    // Enroll student into a course by course code
    @Transactional
    public boolean enrollStudentInCourse(Long studentId, String courseCode) {
        User student = userRepository.findById(studentId).orElseThrow(() -> new RuntimeException("Student not found"));
        Optional<Course> courseOpt = courseRepository.findByCourseCode(courseCode);

        if (courseOpt.isEmpty()) {
            return false; // Course code not found
        }

        Course course = courseOpt.get();

        // Check if already enrolled
        if (student.getEnrolledCourses().contains(course)) {
            return false; // Already enrolled
        }

        // Add course to student's courses
        student.getEnrolledCourses().add(course);
        userRepository.save(student);

        // Add student to course's students
        course.getStudents().add(student);

        return true;
    }
}
