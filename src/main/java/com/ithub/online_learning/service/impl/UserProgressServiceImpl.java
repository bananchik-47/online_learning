package com.ithub.online_learning.service.impl;

import com.ithub.online_learning.dto.request.UserProgressRequest;
import com.ithub.online_learning.dto.response.UserProgressResponse;
import com.ithub.online_learning.entity.Lesson;
import com.ithub.online_learning.entity.User;
import com.ithub.online_learning.entity.UserProgress;
import com.ithub.online_learning.exception.ResourceNotFoundException;
import com.ithub.online_learning.mapper.UserProgressMapper;
import com.ithub.online_learning.repository.LessonRepository;
import com.ithub.online_learning.repository.UserProgressRepository;
import com.ithub.online_learning.repository.UserRepository;
import com.ithub.online_learning.service.UserProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProgressServiceImpl implements UserProgressService {

    private final UserProgressRepository userProgressRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final UserProgressMapper userProgressMapper;

    @Override
    public UserProgressResponse updateProgress(Long userId, Long lessonId, UserProgressRequest request) {
        UserProgress progress = userProgressRepository.findByUserIdAndLessonId(userId, lessonId)
                .orElseGet(() -> createProgress(userId, lessonId));

        userProgressMapper.updateFromRequest(request, progress);

        if (Boolean.TRUE.equals(request.getCompleted())) {
            progress.setCompletedAt(LocalDateTime.now());
        } else {
            progress.setCompletedAt(null);
        }

        return userProgressMapper.toResponse(userProgressRepository.save(progress));
    }

    @Override
    @Transactional(readOnly = true)
    public UserProgressResponse findByUserIdAndLessonId(Long userId, Long lessonId) {
        UserProgress progress = userProgressRepository.findByUserIdAndLessonId(userId, lessonId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Progress not found for user: " + userId + " and lesson: " + lessonId));
        return userProgressMapper.toResponse(progress);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProgressResponse> findByUserId(Long userId) {
        return userProgressMapper.toResponseList(userProgressRepository.findByUserId(userId));
    }

    private UserProgress createProgress(Long userId, Long lessonId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + lessonId));

        return UserProgress.builder()
                .user(user)
                .lesson(lesson)
                .completed(false)
                .build();
    }
}
