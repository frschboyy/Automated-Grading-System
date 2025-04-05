package com.gradingsystem.tesla.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for Assignment entity.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentDTO {

    /** Assignment ID. */
    private Long id;

    /** Title of the assignment. */
    private String title;

    /** Description of the assignment. */
    private String description;

    /** Due date for the assignment. */
    private String dueDate;
}
