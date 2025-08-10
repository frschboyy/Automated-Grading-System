package com.gradingsystem.tesla.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.gradingsystem.tesla.model.Course;
import com.gradingsystem.tesla.model.Institution;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByInstitution(Institution institution);

    // Fetch courses belonging to an institution that do not have a teacher assigned
    @Query("SELECT c FROM Course c WHERE c.institution = :institution AND c.teacher IS NULL")
    List<Course> findUnassignedByInstitution(@Param("institution") Institution institution);

    List<Course> findByInstitutionAndTeacherIsNotNull(Institution institution);

    Course findByIdAndInstitution(Long id, Institution institution);

    @Query("SELECT c FROM Course c WHERE c.teacher.id = :teacherId")
    List<Course> findByTeacherId(@Param("teacherId") Long teacherId);

    Optional<Course> findByCourseCode(String courseCode);
}
