package com.gradingsystem.tesla.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.gradingsystem.tesla.model.Course;
import com.gradingsystem.tesla.model.Institution;
import com.gradingsystem.tesla.repository.CourseRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    // INSTITUTION ADMIN Services

    public List<Course> findByInstitution(Institution institution) {
        return courseRepository.findByInstitution(institution);
    }

    public Course findById(Long id) {
        return courseRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Course not found"));
    }

    public Course saveCourse(Course course) {
        return courseRepository.save(course);
    }

    public List<Course> findUnassignedCoursesForInstitution(Institution institution) {
        return courseRepository.findUnassignedByInstitution(institution);
    }

    public List<Course> findAssignedCoursesForInstitution(Institution institution) {
        return courseRepository.findByInstitutionAndTeacherIsNotNull(institution);
    }

    public Course findByIdAndInstitution(Long id, Institution institution) {
        return courseRepository.findByIdAndInstitution(id, institution);
    }

    // TEACHER Services

    public List<Course> getCoursesForTeacher(Long teacherId) {
        return courseRepository.findByTeacherId(teacherId);
    }

    public void saveAllCourses(Set<Course> enrolledCourses) {
        courseRepository.saveAll(enrolledCourses);
    }

    public String getCourseCode(Long courseId) {
        return courseRepository.findById(courseId).get().getCourseCode();
    }
}
