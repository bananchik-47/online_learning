package com.ithub.online_learning.service;

import com.ithub.online_learning.dto.request.CourseModuleCreateRequest;
import com.ithub.online_learning.dto.request.CourseModuleUpdateRequest;
import com.ithub.online_learning.dto.response.CourseModuleResponse;

import java.util.List;

public interface CourseModuleService {

    CourseModuleResponse create(Long courseId, CourseModuleCreateRequest request);

    CourseModuleResponse update(Long id, CourseModuleUpdateRequest request);

    CourseModuleResponse findById(Long id);

    List<CourseModuleResponse> findByCourseId(Long courseId);

    void delete(Long id);
}
