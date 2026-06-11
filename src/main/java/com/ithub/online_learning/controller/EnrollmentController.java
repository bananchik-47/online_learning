package com.ithub.online_learning.controller;

import com.ithub.online_learning.dto.response.EnrollmentResponse;
import com.ithub.online_learning.security.CustomUserDetails;
import com.ithub.online_learning.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/enrollments")
@RequiredArgsConstructor
@Tag(name = "Enrollments", description = "Course enrollment operations")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/courses/{courseId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Enroll in a course")
    @ApiResponse(responseCode = "201", description = "Enrollment created")
    @ApiResponse(responseCode = "400", description = "Already enrolled or course not available")
    public EnrollmentResponse enroll(@Parameter(description = "Course ID") @PathVariable Long courseId,
                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        return enrollmentService.enroll(userDetails.getId(), courseId);
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Mark enrollment as completed")
    @ApiResponse(responseCode = "200", description = "Enrollment completed")
    @ApiResponse(responseCode = "404", description = "Enrollment not found")
    public EnrollmentResponse complete(@Parameter(description = "Enrollment ID") @PathVariable Long id) {
        return enrollmentService.complete(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get enrollment by ID")
    @ApiResponse(responseCode = "200", description = "Enrollment found")
    @ApiResponse(responseCode = "404", description = "Enrollment not found")
    public EnrollmentResponse findById(@Parameter(description = "Enrollment ID") @PathVariable Long id) {
        return enrollmentService.findById(id);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get current user's enrollments")
    @ApiResponse(responseCode = "200", description = "List of user enrollments")
    public List<EnrollmentResponse> findByCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return enrollmentService.findByUserId(userDetails.getId());
    }

    @GetMapping("/courses/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all enrollments for a course")
    @ApiResponse(responseCode = "200", description = "List of course enrollments")
    public List<EnrollmentResponse> findByCourseId(@Parameter(description = "Course ID") @PathVariable Long courseId) {
        return enrollmentService.findByCourseId(courseId);
    }
}
