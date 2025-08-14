package com.gradingsystem.tesla.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.gradingsystem.tesla.dto.AssignmentDTO;
import com.gradingsystem.tesla.service.AssignmentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    // Get single assignment details
    @GetMapping("/{assignmentId}")
    public ResponseEntity<AssignmentDTO> getAssignmentById(@PathVariable Long assignmentId) {
        AssignmentDTO dto = assignmentService.getAssignmentById(assignmentId);
        return ResponseEntity.ok(dto);
    }
}
