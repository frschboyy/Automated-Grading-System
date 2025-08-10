package com.gradingsystem.tesla.model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "institution", "enrolledCourses", "teachingCourses" })
@ToString(exclude = { "institution", "enrolledCourses", "teachingCourses" })
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(unique = true, nullable = true) // can be null for INSTITUTION ADMIN and ADMINS
    private String registrationId;

    @Column(nullable = false)
    private String firstName;

    private String middleName;
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean verified;

    @Column(nullable = false)
    private boolean approvedByAdmin;

    @ManyToOne
    @JoinColumn(name = "institution_id", nullable = true)
    @JsonBackReference
    private Institution institution;

    @Column(nullable = false)
    private String role; // STUDENT, TEACHER

    // Only for students
    @ManyToMany(mappedBy = "students")
    private Set<Course> enrolledCourses;

    // Only for teachers
    @OneToMany(mappedBy = "teacher")
    private Set<Course> teachingCourses;
}