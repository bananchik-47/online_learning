package com.ithub.online_learning.controller.web.support;

import com.ithub.online_learning.dto.request.AssignmentUpdateRequest;
import com.ithub.online_learning.dto.request.CourseModuleUpdateRequest;
import com.ithub.online_learning.dto.request.CourseUpdateRequest;
import com.ithub.online_learning.dto.request.LessonUpdateRequest;
import com.ithub.online_learning.dto.response.AssignmentResponse;
import com.ithub.online_learning.dto.response.CourseModuleResponse;
import com.ithub.online_learning.dto.response.CourseResponse;
import com.ithub.online_learning.dto.response.LessonResponse;
import org.springframework.stereotype.Component;

@Component
public class AdminFormMapper {

    public CourseUpdateRequest toCourseUpdateRequest(CourseResponse course) {
        return CourseUpdateRequest.builder()
                .title(course.getTitle())
                .description(course.getDescription())
                .isPublished(course.getIsPublished())
                .build();
    }

    public CourseModuleUpdateRequest toCourseModuleUpdateRequest(CourseModuleResponse module) {
        return CourseModuleUpdateRequest.builder()
                .title(module.getTitle())
                .description(module.getDescription())
                .orderIndex(module.getOrderIndex())
                .build();
    }

    public LessonUpdateRequest toLessonUpdateRequest(LessonResponse lesson) {
        return LessonUpdateRequest.builder()
                .title(lesson.getTitle())
                .content(lesson.getContent())
                .orderIndex(lesson.getOrderIndex())
                .build();
    }

    public AssignmentUpdateRequest toAssignmentUpdateRequest(AssignmentResponse assignment) {
        return AssignmentUpdateRequest.builder()
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .maxScore(assignment.getMaxScore())
                .dueDate(assignment.getDueDate())
                .build();
    }
}
