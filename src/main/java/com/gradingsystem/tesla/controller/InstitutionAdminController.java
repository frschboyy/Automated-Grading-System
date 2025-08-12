package com.gradingsystem.tesla.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gradingsystem.tesla.dto.CourseDTO;
import com.gradingsystem.tesla.model.Course;
import com.gradingsystem.tesla.model.User;
import com.gradingsystem.tesla.service.CourseService;
import com.gradingsystem.tesla.service.EmailService;
import com.gradingsystem.tesla.service.UserService;
import com.gradingsystem.tesla.util.CustomUserDetails;
import com.gradingsystem.tesla.util.PasswordGenerator;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/institution-admin")
@PreAuthorize("hasRole('INSTITUTION_ADMIN')")
@RequiredArgsConstructor
public class InstitutionAdminController {

    private final UserService userService;
    private final CourseService courseService;
    private final EmailService emailService;

    // MANAGE TEACHERS

    @GetMapping("/manage-teachers")
    public String showTeachersPage(Model model,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        // Get logged-in admin user
        User loggedInAdmin = currentUser.getUser();

        // Query list of teachers belonging to the institution
        List<User> teachers = userService.findUsersForInstitution(loggedInAdmin.getInstitution(), "TEACHER");

        // Query list of unassigned courses belonging to the institution
        List<Course> unassignedCourses = courseService.findUnassignedCoursesForInstitution(
                loggedInAdmin.getInstitution());

        // Query list of assigned courses belonging to the institution (teacher not
        // null)
        List<Course> assignedCourses = courseService.findAssignedCoursesForInstitution(
                loggedInAdmin.getInstitution());

        model.addAttribute("teachers", teachers);
        model.addAttribute("courses", unassignedCourses); // for assign dropdown
        model.addAttribute("assignedCourses", assignedCourses); // for unassign dropdown
        return "manageTeachers";
    }

    @PostMapping("/manage-teachers/add")
    public String addTeacher(@Valid @ModelAttribute User teacher,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        // Get logged-in admin user
        User loggedInAdmin = currentUser.getUser();

        // Generate Password for Teacher User
        String rawPassword = PasswordGenerator.generateAdminPassword();

        teacher.setRole("TEACHER");
        teacher.setVerified(true);
        teacher.setApprovedByAdmin(true);
        teacher.setPassword(rawPassword);
        teacher.setInstitution(loggedInAdmin.getInstitution());

        // Save Teacher User
        userService.saveUser(teacher);

        // Send Approval email to teacher
        String name = teacher.getFirstName() + " " + teacher.getMiddleName() + " " + teacher.getLastName();
        emailService.sendApprovalEmail(name, teacher.getEmail(), rawPassword, teacher.getRole());

        return "redirect:/institution-admin/manage-teachers?success=true";
    }

    @DeleteMapping("/manage-teachers/remove/{registrationId}")
    @ResponseBody
    public ResponseEntity<?> removeTeacher(@PathVariable String registrationId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        User loggedInAdmin = currentUser.getUser();
        User teacher = userService.findByRegistrationIdAndInstitution(registrationId, loggedInAdmin.getInstitution());

        if (teacher == null || !"TEACHER".equals(teacher.getRole())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Teacher not found in your institution"));
        }

        // Check if teacher has active courses
        if (!teacher.getTeachingCourses().isEmpty()) {

            List<CourseDTO> dto = teacher.getTeachingCourses().stream()
                    .map(course -> new CourseDTO(course.getId(), course.getName(), course.getCourseCode()))
                    .toList();

            return ResponseEntity.ok(Map.of(
                    "hasActiveCourses", true,
                    "courses", dto));
        }

        // Store email
        String email = teacher.getEmail();

        userService.removeUser(teacher);

        // Send removal email to teacher
        emailService.sendRemovalEmail(email);
        return ResponseEntity.ok(Map.of("message", "Teacher removed successfully"));
    }

    @PostMapping("/manage-teachers/unassign-and-remove")
    @ResponseBody
    public ResponseEntity<?> unassignAndRemoveTeacher(@RequestParam String registrationId) {
        User teacher = userService.findByRegistrationId(registrationId);
        if (teacher == null || !"TEACHER".equals(teacher.getRole())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Teacher not found"));
        }

        // Store email
        String email = teacher.getEmail();

        // Mark all courses as unassigned
        teacher.getTeachingCourses().forEach(course -> course.setTeacher(null));
        userService.removeUser(teacher);

        // Send removal email to teacher
        emailService.sendRemovalEmail(email);
        return ResponseEntity.ok(Map.of("message", "Teacher unassigned and removed successfully"));
    }

    @PostMapping("/manage-teachers/assign-course")
    public String assignTeacherToCourse(@RequestParam String registrationId,
            @RequestParam Long courseId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        User loggedInAdmin = currentUser.getUser();
        User teacher = userService.findByRegistrationIdAndInstitution(registrationId, loggedInAdmin.getInstitution());
        Course course = courseService.findByIdAndInstitution(courseId, loggedInAdmin.getInstitution());

        if (teacher == null || !"TEACHER".equals(teacher.getRole())) {
            return "redirect:/institution-admin/manage-teachers?error=teacherNotFound";
        }
        if (course == null) {
            return "redirect:/institution-admin/manage-teachers?error=courseNotFound";
        }

        // Assign teacher to course
        course.assignTeacher(teacher);
        courseService.saveCourse(course);

        return "redirect:/institution-admin/manage-teachers?success=teacherAssigned";
    }

    @PostMapping("/manage-teachers/unassign")
    public String unassignTeacherFromCourse(@RequestParam Long courseId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        User loggedInAdmin = currentUser.getUser();
        Course course = courseService.findByIdAndInstitution(courseId, loggedInAdmin.getInstitution());

        if (course == null) {
            return "redirect:/institution-admin/manage-teachers?error=invalidCourse";
        }

        course.unassignTeacher();
        courseService.saveCourse(course);

        return "redirect:/institution-admin/manage-teachers?success=unassigned";
    }

    // MANAGE COURSES

    @GetMapping("/manage-courses")
    public String showCoursesPage(Model model,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        User loggedInAdmin = currentUser.getUser();
        List<Course> courses = courseService.findByInstitution(loggedInAdmin.getInstitution());
        model.addAttribute("courses", courses);
        return "manageCourses";
    }

    @PostMapping("/manage-courses/add")
    public String addCourse(@RequestParam String code,
            @RequestParam String name,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        User loggedInAdmin = currentUser.getUser();
        Course course = new Course();
        course.setCourseCode(code);
        course.setName(name);
        course.setInstitution(loggedInAdmin.getInstitution());
        courseService.saveCourse(course);

        return "redirect:/institution-admin/manage-courses?success=true";
    }

    // MANAGE STUDENTS

    @GetMapping("/manage-students")
    public String showStudentsPage(Model model,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        User loggedInAdmin = currentUser.getUser();
        List<User> students = userService.findUsersForInstitution(loggedInAdmin.getInstitution(), "STUDENT");
        List<User> pendingStudents = userService.findPendingStudents(loggedInAdmin.getInstitution());
        model.addAttribute("students", students);
        model.addAttribute("pendingStudents", pendingStudents);
        return "manageStudents";
    }

    @PostMapping("/manage-students/add")
    public String addStudent(@Valid @ModelAttribute User student,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        User loggedInAdmin = currentUser.getUser();
        String rawPassword = PasswordGenerator.generateAdminPassword();
        student.setRole("STUDENT");
        student.setVerified(true);
        student.setApprovedByAdmin(true);
        student.setPassword(rawPassword);
        student.setInstitution(loggedInAdmin.getInstitution());

        userService.saveUser(student);
        String name = student.getFirstName() + " " + student.getMiddleName() + " " + student.getLastName();
        emailService.sendApprovalEmail(name, student.getEmail(), rawPassword, student.getRole());
        return "redirect:/institution-admin/manage-students?success=true";
    }

    @DeleteMapping("/manage-students/remove/{registrationId}")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> removeStudent(@PathVariable String registrationId) {
        User student = userService.findByRegistrationId(registrationId);

        if (student == null || !"STUDENT".equals(student.getRole())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Student not found"));
        }

        // Unenroll from all courses
        for (Course course : new HashSet<>(student.getEnrolledCourses())) {
            course.getStudents().remove(student); // Remove student from course
            student.getEnrolledCourses().remove(course); // Remove course from student
        }

        // Persist updates
        courseService.saveAllCourses(student.getEnrolledCourses());

        // Finally delete student
        userService.removeUser(student);

        emailService.sendRemovalEmail(student.getEmail());
        return ResponseEntity.ok(Map.of("message", "Student unenrolled and removed successfully"));
    }

    @PostMapping("/manage-students/approve")
    @ResponseBody
    public ResponseEntity<?> approveStudent(@RequestParam Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        User loggedInAdmin = currentUser.getUser();
        User student = userService.findByIdAndInstitution(id, loggedInAdmin.getInstitution());

        if (student == null || !"STUDENT".equals(student.getRole())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Student not found in your institution"));
        }

        student.setApprovedByAdmin(true);
        userService.saveUser(student);
        emailService.sendApprovalEmail(student.getFirstName(), student.getEmail(), null, "STUDENT");
        return ResponseEntity.ok(Map.of("message", "Student approved"));
    }

    @PostMapping("/manage-students/deny")
    @ResponseBody
    public ResponseEntity<?> denyStudent(@RequestParam Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        User loggedInAdmin = currentUser.getUser();
        User student = userService.findByIdAndInstitution(id, loggedInAdmin.getInstitution());

        if (student == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Student not found in your institution"));
        }
        userService.removeUser(student);
        emailService.sendDenialEmail(student.getEmail());
        return ResponseEntity.ok(Map.of("message", "Student denied and removed"));
    }
}
