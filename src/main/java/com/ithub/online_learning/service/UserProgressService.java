package com.ithub.online_learning.service;

import com.ithub.online_learning.dto.request.UserProgressRequest;
import com.ithub.online_learning.dto.response.UserProgressResponse;

import java.util.List;

public interface UserProgressService {

    UserProgressResponse updateProgress(Long userId, Long lessonId, UserProgressRequest request);

    UserProgressResponse findByUserIdAndLessonId(Long userId, Long lessonId);

    List<UserProgressResponse> findByUserId(Long userId);
}
