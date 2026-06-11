package com.ithub.online_learning.controller;

import com.ithub.online_learning.dto.request.SubmissionGradeRequest;
import com.ithub.online_learning.dto.request.SubmissionRequest;
import com.ithub.online_learning.dto.response.SubmissionResponse;
import com.ithub.online_learning.security.CustomUserDetails;
import com.ithub.online_learning.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/submissions")
@RequiredArgsConstructor
@Tag(name = "Submissions", description = "Assignment submission and grading operations")
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping("/assignments/{assignmentId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Submit an assignment")
    @ApiResponse(responseCode = "201", description = "Submission created")
    @ApiResponse(responseCode = "400", description = "Submission already exists")
    public SubmissionResponse submit(@Parameter(description = "Assignment ID") @PathVariable Long assignmentId,
                                     @Valid @RequestBody SubmissionRequest request,
                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        return submissionService.submit(assignmentId, userDetails.getId(), request);
    }

    @PutMapping("/assignments/{assignmentId}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Update a submission")
    @ApiResponse(responseCode = "200", description = "Submission updated")
    @ApiResponse(responseCode = "404", description = "Submission not found")
    public SubmissionResponse update(@Parameter(description = "Assignment ID") @PathVariable Long assignmentId,
                                     @Valid @RequestBody SubmissionRequest request,
                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        return submissionService.update(assignmentId, userDetails.getId(), request);
    }

    @PutMapping("/{id}/grade")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Grade a submission")
    @ApiResponse(responseCode = "200", description = "Submission graded")
    @ApiResponse(responseCode = "404", description = "Submission not found")
    public SubmissionResponse grade(@Parameter(description = "Submission ID") @PathVariable Long id,
                                    @Valid @RequestBody SubmissionGradeRequest request) {
        return submissionService.grade(id, request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get submission by ID")
    @ApiResponse(responseCode = "200", description = "Submission found")
    @ApiResponse(responseCode = "404", description = "Submission not found")
    public SubmissionResponse findById(@Parameter(description = "Submission ID") @PathVariable Long id) {
        return submissionService.findById(id);
    }

    @GetMapping("/assignments/{assignmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all submissions for an assignment")
    @ApiResponse(responseCode = "200", description = "List of submissions")
    public List<SubmissionResponse> findByAssignmentId(@Parameter(description = "Assignment ID") @PathVariable Long assignmentId) {
        return submissionService.findByAssignmentId(assignmentId);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get current student's submissions")
    @ApiResponse(responseCode = "200", description = "List of student submissions")
    public List<SubmissionResponse> findByCurrentStudent(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return submissionService.findByStudentId(userDetails.getId());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get submissions pending grading")
    @ApiResponse(responseCode = "200", description = "List of pending submissions")
    public List<SubmissionResponse> findPendingGrading() {
        return submissionService.findPendingGrading();
    }
}
