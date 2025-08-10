package com.gradingsystem.tesla.repository;

import com.gradingsystem.tesla.model.Assignment;

// import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    // @Query("SELECT a.rubricDocument FROM Assignment a WHERE a.id = :id")
    // byte[] findRubricDocumentById(@Param("id") Long id);

    // Fetch all assignments with a due date in the future
    @Query("SELECT a FROM Assignment a WHERE a.dueDate > CURRENT_DATE")
    List<Assignment> findAllUpcomingAssignments();

    @Query("SELECT a FROM Assignment a WHERE a.id = :id")
    Assignment findAssignmentById(@Param("id") Long id);

    @Query("SELECT a FROM Assignment a WHERE a.course.id = :courseId AND a.course.teacher.id = :teacherId")
    List<Assignment> findByTeacherAndCourse(@Param("teacherId") Long teacherId,
            @Param("courseId") Long courseId);

    // List<Assignment> findByCourseIdAndDueDateAfter(Long courseId, LocalDateTime dueDate);

    List<Assignment> findByCourseId(Long courseId);
}
