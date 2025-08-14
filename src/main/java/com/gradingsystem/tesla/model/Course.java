package com.gradingsystem.tesla.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "courses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "students" })
@ToString(exclude = { "students" })
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String courseCode;

    @Column(nullable = false)
    private String name;

    // Teacher
    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = true)
    private User teacher;

    // Students enrolled
    @Builder.Default
    @ManyToMany
    @JoinTable(name = "course_student", joinColumns = @JoinColumn(name = "course_id"), inverseJoinColumns = @JoinColumn(name = "student_id"))
    private Set<User> students = new HashSet<>();

    // Assignments
    @Builder.Default
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Assignment> assignments = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    // Helper methods ----

    public void assignTeacher(User teacher) {
        this.teacher = teacher;
        if (teacher != null) {
            if (teacher.getTeachingCourses() == null) {
                teacher.setTeachingCourses(new HashSet<>());
            }
            teacher.getTeachingCourses().add(this);
        }
    }

    public void unassignTeacher() {
        if (this.teacher != null) {
            this.teacher.getTeachingCourses().remove(this);
            this.teacher = null;
        }
    }

}
