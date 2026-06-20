package com.ithub.online_learning.service;

import com.ithub.online_learning.dto.request.LessonCreateRequest;
import com.ithub.online_learning.dto.response.LessonResponse;
import com.ithub.online_learning.entity.CourseModule;
import com.ithub.online_learning.entity.Lesson;
import com.ithub.online_learning.exception.ResourceNotFoundException;
import com.ithub.online_learning.mapper.LessonMapper;
import com.ithub.online_learning.repository.CourseModuleRepository;
import com.ithub.online_learning.repository.LessonRepository;
import com.ithub.online_learning.service.impl.LessonServiceImpl;
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
class LessonServiceImplTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private CourseModuleRepository courseModuleRepository;

    @Mock
    private LessonMapper lessonMapper;

    @InjectMocks
    private LessonServiceImpl lessonService;

    @Test
    void create_assignsModuleAndReturnsResponse() {
        CourseModule module = CourseModule.builder().id(5L).title("Module 1").build();
        LessonCreateRequest request = LessonCreateRequest.builder()
                .title("Lesson 1")
                .content("Content")
                .orderIndex(0)
                .build();
        Lesson lesson = Lesson.builder().title("Lesson 1").build();
        Lesson saved = Lesson.builder().id(20L).title("Lesson 1").module(module).build();
        LessonResponse response = LessonResponse.builder().id(20L).title("Lesson 1").build();

        when(courseModuleRepository.findById(5L)).thenReturn(Optional.of(module));
        when(lessonMapper.toEntity(request)).thenReturn(lesson);
        when(lessonRepository.save(lesson)).thenReturn(saved);
        when(lessonMapper.toResponse(saved)).thenReturn(response);

        LessonResponse result = lessonService.create(5L, request);

        assertThat(result.getTitle()).isEqualTo("Lesson 1");
        assertThat(lesson.getModule()).isEqualTo(module);
    }

    @Test
    void create_missingModule_throwsNotFound() {
        LessonCreateRequest request = LessonCreateRequest.builder()
                .title("Lesson 1")
                .orderIndex(0)
                .build();

        when(courseModuleRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonService.create(5L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course module not found");
    }

    @Test
    void delete_existingLesson_removesRecord() {
        when(lessonRepository.existsById(20L)).thenReturn(true);

        lessonService.delete(20L);

        verify(lessonRepository).deleteById(20L);
    }

    @Test
    void findById_missingLesson_throwsNotFound() {
        when(lessonRepository.findByIdWithDetails(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonService.findById(20L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Lesson not found");
    }
}
