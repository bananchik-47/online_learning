package com.ithub.online_learning.service;

import com.ithub.online_learning.dto.request.CourseCreateRequest;
import com.ithub.online_learning.dto.request.CourseUpdateRequest;
import com.ithub.online_learning.dto.response.CourseDetailResponse;
import com.ithub.online_learning.dto.response.CourseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CourseService {

    CourseResponse create(CourseCreateRequest request, Long instructorId);

    CourseResponse update(Long id, CourseUpdateRequest request);

    CourseResponse findById(Long id);

    CourseDetailResponse findDetailById(Long id);

    Page<CourseResponse> findPublished(Pageable pageable);

    List<CourseResponse> findByInstructor(Long instructorId);

    void delete(Long id);
}
