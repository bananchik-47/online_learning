package com.ithub.online_learning.controller;

import com.ithub.online_learning.dto.request.AssignmentCreateRequest;
import com.ithub.online_learning.dto.request.AssignmentUpdateRequest;
import com.ithub.online_learning.dto.response.AssignmentResponse;
import com.ithub.online_learning.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Assignments", description = "Assignment management operations")
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping("/lessons/{lessonId}/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create an assignment")
    @ApiResponse(responseCode = "201", description = "Assignment created")
    public AssignmentResponse create(@Parameter(description = "Lesson ID") @PathVariable Long lessonId,
                                     @Valid @RequestBody AssignmentCreateRequest request) {
        return assignmentService.create(lessonId, request);
    }

    @PutMapping("/assignments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an assignment")
    @ApiResponse(responseCode = "200", description = "Assignment updated")
    @ApiResponse(responseCode = "404", description = "Assignment not found")
    public AssignmentResponse update(@Parameter(description = "Assignment ID") @PathVariable Long id,
                                     @Valid @RequestBody AssignmentUpdateRequest request) {
        return assignmentService.update(id, request);
    }

    @GetMapping("/assignments/{id}")
    @Operation(summary = "Get assignment by ID")
    @ApiResponse(responseCode = "200", description = "Assignment found")
    @ApiResponse(responseCode = "404", description = "Assignment not found")
    public AssignmentResponse findById(@Parameter(description = "Assignment ID") @PathVariable Long id) {
        return assignmentService.findById(id);
    }

    @GetMapping("/lessons/{lessonId}/assignments")
    @Operation(summary = "Get all assignments of a lesson")
    @ApiResponse(responseCode = "200", description = "List of assignments")
    public List<AssignmentResponse> findByLessonId(@Parameter(description = "Lesson ID") @PathVariable Long lessonId) {
        return assignmentService.findByLessonId(lessonId);
    }

    @DeleteMapping("/assignments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete an assignment")
    @ApiResponse(responseCode = "204", description = "Assignment deleted")
    @ApiResponse(responseCode = "404", description = "Assignment not found")
    public void delete(@Parameter(description = "Assignment ID") @PathVariable Long id) {
        assignmentService.delete(id);
    }
}
