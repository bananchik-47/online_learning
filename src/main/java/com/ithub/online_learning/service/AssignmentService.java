package com.ithub.online_learning.service;

import com.ithub.online_learning.dto.request.AssignmentCreateRequest;
import com.ithub.online_learning.dto.request.AssignmentUpdateRequest;
import com.ithub.online_learning.dto.response.AssignmentResponse;

import java.util.List;

public interface AssignmentService {

    AssignmentResponse create(Long lessonId, AssignmentCreateRequest request);

    AssignmentResponse update(Long id, AssignmentUpdateRequest request);

    AssignmentResponse findById(Long id);

    List<AssignmentResponse> findByLessonId(Long lessonId);

    void delete(Long id);
}
