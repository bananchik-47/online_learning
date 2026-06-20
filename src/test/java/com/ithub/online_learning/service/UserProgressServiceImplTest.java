package com.ithub.online_learning.service;

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
import com.ithub.online_learning.service.impl.UserProgressServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProgressServiceImplTest {

    @Mock
    private UserProgressRepository userProgressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private UserProgressMapper userProgressMapper;

    @InjectMocks
    private UserProgressServiceImpl userProgressService;

    @Test
    void updateProgress_newRecord_createsAndMarksCompleted() {
        User user = User.builder().id(1L).build();
        Lesson lesson = Lesson.builder().id(20L).title("Intro").build();
        UserProgress saved = UserProgress.builder()
                .id(300L)
                .user(user)
                .lesson(lesson)
                .completed(true)
                .build();
        UserProgressResponse response = UserProgressResponse.builder()
                .id(300L)
                .lessonId(20L)
                .completed(true)
                .build();

        when(userProgressRepository.findByUserIdAndLessonId(1L, 20L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(lessonRepository.findById(20L)).thenReturn(Optional.of(lesson));
        when(userProgressRepository.save(any(UserProgress.class))).thenReturn(saved);
        when(userProgressMapper.toResponse(saved)).thenReturn(response);

        UserProgressResponse result = userProgressService.updateProgress(
                1L, 20L, UserProgressRequest.builder().completed(true).build());

        assertThat(result.getCompleted()).isTrue();
        verify(userProgressMapper).updateFromRequest(any(UserProgressRequest.class), any(UserProgress.class));
    }

    @Test
    void updateProgress_existingRecord_clearsCompletedAtWhenNotCompleted() {
        UserProgress progress = UserProgress.builder()
                .id(300L)
                .completed(true)
                .build();
        UserProgressResponse response = UserProgressResponse.builder()
                .id(300L)
                .completed(false)
                .build();

        when(userProgressRepository.findByUserIdAndLessonId(1L, 20L)).thenReturn(Optional.of(progress));
        when(userProgressRepository.save(progress)).thenReturn(progress);
        when(userProgressMapper.toResponse(progress)).thenReturn(response);

        userProgressService.updateProgress(1L, 20L, UserProgressRequest.builder().completed(false).build());

        assertThat(progress.getCompletedAt()).isNull();
    }

    @Test
    void findByUserIdAndLessonId_missingProgress_throwsNotFound() {
        when(userProgressRepository.findByUserIdAndLessonId(1L, 20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userProgressService.findByUserIdAndLessonId(1L, 20L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Progress not found");
    }
}
