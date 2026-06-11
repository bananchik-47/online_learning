package com.ithub.online_learning.controller;

import com.ithub.online_learning.dto.request.UserProgressRequest;
import com.ithub.online_learning.dto.response.UserProgressResponse;
import com.ithub.online_learning.security.CustomUserDetails;
import com.ithub.online_learning.service.UserProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/progress")
@RequiredArgsConstructor
@Tag(name = "User Progress", description = "Lesson progress tracking operations")
public class UserProgressController {

    private final UserProgressService userProgressService;

    @PutMapping("/lessons/{lessonId}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Update lesson progress")
    @ApiResponse(responseCode = "200", description = "Progress updated")
    @ApiResponse(responseCode = "404", description = "Lesson not found")
    public UserProgressResponse updateProgress(@Parameter(description = "Lesson ID") @PathVariable Long lessonId,
                                               @Valid @RequestBody UserProgressRequest request,
                                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        return userProgressService.updateProgress(userDetails.getId(), lessonId, request);
    }

    @GetMapping("/lessons/{lessonId}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get progress for a specific lesson")
    @ApiResponse(responseCode = "200", description = "Progress found")
    @ApiResponse(responseCode = "404", description = "Progress not found")
    public UserProgressResponse findByLessonId(@Parameter(description = "Lesson ID") @PathVariable Long lessonId,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        return userProgressService.findByUserIdAndLessonId(userDetails.getId(), lessonId);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get all progress records for current user")
    @ApiResponse(responseCode = "200", description = "List of progress records")
    public List<UserProgressResponse> findByCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return userProgressService.findByUserId(userDetails.getId());
    }
}
