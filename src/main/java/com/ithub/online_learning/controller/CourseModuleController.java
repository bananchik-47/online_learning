package com.ithub.online_learning.controller;

import com.ithub.online_learning.dto.request.CourseModuleCreateRequest;
import com.ithub.online_learning.dto.request.CourseModuleUpdateRequest;
import com.ithub.online_learning.dto.response.CourseModuleResponse;
import com.ithub.online_learning.service.CourseModuleService;
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
@Tag(name = "Course Modules", description = "Course module management operations")
public class CourseModuleController {

    private final CourseModuleService courseModuleService;

    @PostMapping("/courses/{courseId}/modules")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a course module")
    @ApiResponse(responseCode = "201", description = "Module created")
    public CourseModuleResponse create(@Parameter(description = "Course ID") @PathVariable Long courseId,
                                       @Valid @RequestBody CourseModuleCreateRequest request) {
        return courseModuleService.create(courseId, request);
    }

    @PutMapping("/modules/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a course module")
    @ApiResponse(responseCode = "200", description = "Module updated")
    @ApiResponse(responseCode = "404", description = "Module not found")
    public CourseModuleResponse update(@Parameter(description = "Module ID") @PathVariable Long id,
                                       @Valid @RequestBody CourseModuleUpdateRequest request) {
        return courseModuleService.update(id, request);
    }

    @GetMapping("/modules/{id}")
    @Operation(summary = "Get course module by ID")
    @ApiResponse(responseCode = "200", description = "Module found")
    @ApiResponse(responseCode = "404", description = "Module not found")
    public CourseModuleResponse findById(@Parameter(description = "Module ID") @PathVariable Long id) {
        return courseModuleService.findById(id);
    }

    @GetMapping("/courses/{courseId}/modules")
    @Operation(summary = "Get all modules of a course")
    @ApiResponse(responseCode = "200", description = "List of course modules")
    public List<CourseModuleResponse> findByCourseId(@Parameter(description = "Course ID") @PathVariable Long courseId) {
        return courseModuleService.findByCourseId(courseId);
    }

    @DeleteMapping("/modules/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a course module")
    @ApiResponse(responseCode = "204", description = "Module deleted")
    @ApiResponse(responseCode = "404", description = "Module not found")
    public void delete(@Parameter(description = "Module ID") @PathVariable Long id) {
        courseModuleService.delete(id);
    }
}
