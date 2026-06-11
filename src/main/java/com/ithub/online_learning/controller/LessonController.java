package com.ithub.online_learning.controller;

import com.ithub.online_learning.dto.request.LessonCreateRequest;
import com.ithub.online_learning.dto.request.LessonUpdateRequest;
import com.ithub.online_learning.dto.response.LessonResponse;
import com.ithub.online_learning.dto.response.LessonSummaryResponse;
import com.ithub.online_learning.service.LessonService;
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
@Tag(name = "Lessons", description = "Lesson management operations")
public class LessonController {

    private final LessonService lessonService;

    @PostMapping("/modules/{moduleId}/lessons")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a lesson")
    @ApiResponse(responseCode = "201", description = "Lesson created")
    public LessonResponse create(@Parameter(description = "Module ID") @PathVariable Long moduleId,
                                 @Valid @RequestBody LessonCreateRequest request) {
        return lessonService.create(moduleId, request);
    }

    @PutMapping("/lessons/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a lesson")
    @ApiResponse(responseCode = "200", description = "Lesson updated")
    @ApiResponse(responseCode = "404", description = "Lesson not found")
    public LessonResponse update(@Parameter(description = "Lesson ID") @PathVariable Long id,
                                 @Valid @RequestBody LessonUpdateRequest request) {
        return lessonService.update(id, request);
    }

    @GetMapping("/lessons/{id}")
    @Operation(summary = "Get lesson by ID")
    @ApiResponse(responseCode = "200", description = "Lesson found")
    @ApiResponse(responseCode = "404", description = "Lesson not found")
    public LessonResponse findById(@Parameter(description = "Lesson ID") @PathVariable Long id) {
        return lessonService.findById(id);
    }

    @GetMapping("/modules/{moduleId}/lessons")
    @Operation(summary = "Get all lessons of a module")
    @ApiResponse(responseCode = "200", description = "List of lessons")
    public List<LessonSummaryResponse> findByModuleId(@Parameter(description = "Module ID") @PathVariable Long moduleId) {
        return lessonService.findByModuleId(moduleId);
    }

    @DeleteMapping("/lessons/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a lesson")
    @ApiResponse(responseCode = "204", description = "Lesson deleted")
    @ApiResponse(responseCode = "404", description = "Lesson not found")
    public void delete(@Parameter(description = "Lesson ID") @PathVariable Long id) {
        lessonService.delete(id);
    }
}
