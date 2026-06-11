package com.ithub.online_learning.service;

import com.ithub.online_learning.dto.response.EnrollmentResponse;

import java.util.List;

public interface EnrollmentService {

    EnrollmentResponse enroll(Long userId, Long courseId);

    EnrollmentResponse complete(Long id);

    EnrollmentResponse findById(Long id);

    List<EnrollmentResponse> findByUserId(Long userId);

    List<EnrollmentResponse> findByCourseId(Long courseId);
}
