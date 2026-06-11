package com.ithub.online_learning.controller;

import com.ithub.online_learning.dto.request.CourseCreateRequest;
import com.ithub.online_learning.dto.request.CourseUpdateRequest;
import com.ithub.online_learning.dto.response.CourseDetailResponse;
import com.ithub.online_learning.dto.response.CourseResponse;
import com.ithub.online_learning.security.CustomUserDetails;
import com.ithub.online_learning.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/courses")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "Course management operations")
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    @Operation(summary = "Get published courses", description = "Returns a paginated list of published courses")
    @ApiResponse(responseCode = "200", description = "List of published courses")
    public Page<CourseResponse> findPublished(@ParameterObject Pageable pageable) {
        return courseService.findPublished(pageable);
    }

    @GetMapping("/instructor/me")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get courses of the current instructor")
    @ApiResponse(responseCode = "200", description = "List of instructor courses")
    public List<CourseResponse> findByCurrentInstructor(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return courseService.findByInstructor(userDetails.getId());
    }

    @GetMapping("/{id}/detail")
    @Operation(summary = "Get course with modules and lessons")
    @ApiResponse(responseCode = "200", description = "Course detail found")
    @ApiResponse(responseCode = "404", description = "Course not found")
    public CourseDetailResponse findDetailById(@Parameter(description = "Course ID") @PathVariable Long id) {
        return courseService.findDetailById(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get course by ID")
    @ApiResponse(responseCode = "200", description = "Course found")
    @ApiResponse(responseCode = "404", description = "Course not found")
    public CourseResponse findById(@Parameter(description = "Course ID") @PathVariable Long id) {
        return courseService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new course")
    @ApiResponse(responseCode = "201", description = "Course created")
    public CourseResponse create(@Valid @RequestBody CourseCreateRequest request,
                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        return courseService.create(request, userDetails.getId());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a course")
    @ApiResponse(responseCode = "200", description = "Course updated")
    @ApiResponse(responseCode = "404", description = "Course not found")
    public CourseResponse update(@Parameter(description = "Course ID") @PathVariable Long id,
                                 @Valid @RequestBody CourseUpdateRequest request) {
        return courseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a course")
    @ApiResponse(responseCode = "204", description = "Course deleted")
    @ApiResponse(responseCode = "404", description = "Course not found")
    public void delete(@Parameter(description = "Course ID") @PathVariable Long id) {
        courseService.delete(id);
    }
}
