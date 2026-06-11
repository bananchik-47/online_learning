package com.ithub.online_learning.service;

import com.ithub.online_learning.dto.request.LessonCreateRequest;
import com.ithub.online_learning.dto.request.LessonUpdateRequest;
import com.ithub.online_learning.dto.response.LessonResponse;
import com.ithub.online_learning.dto.response.LessonSummaryResponse;

import java.util.List;

public interface LessonService {

    LessonResponse create(Long moduleId, LessonCreateRequest request);

    LessonResponse update(Long id, LessonUpdateRequest request);

    LessonResponse findById(Long id);

    List<LessonSummaryResponse> findByModuleId(Long moduleId);

    void delete(Long id);
}
