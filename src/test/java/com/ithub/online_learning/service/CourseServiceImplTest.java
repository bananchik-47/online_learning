package com.ithub.online_learning.service;

import com.ithub.online_learning.dto.request.CourseCreateRequest;
import com.ithub.online_learning.dto.response.CourseResponse;
import com.ithub.online_learning.entity.Course;
import com.ithub.online_learning.entity.User;
import com.ithub.online_learning.exception.ResourceNotFoundException;
import com.ithub.online_learning.mapper.CourseMapper;
import com.ithub.online_learning.mapper.CourseModuleMapper;
import com.ithub.online_learning.repository.CourseModuleRepository;
import com.ithub.online_learning.repository.CourseRepository;
import com.ithub.online_learning.repository.UserRepository;
import com.ithub.online_learning.service.impl.CourseServiceImpl;
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
class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseModuleRepository courseModuleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private CourseModuleMapper courseModuleMapper;

    @InjectMocks
    private CourseServiceImpl courseService;

    @Test
    void create_setsInstructorAndDefaultsUnpublishedWhenNull() {
        CourseCreateRequest request = CourseCreateRequest.builder()
                .title("Java Basics")
                .description("Intro")
                .build();
        User instructor = User.builder().id(1L).username("admin").build();
        Course course = Course.builder().title("Java Basics").build();
        Course saved = Course.builder().id(10L).title("Java Basics").isPublished(false).instructor(instructor).build();
        CourseResponse response = CourseResponse.builder().id(10L).title("Java Basics").isPublished(false).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(courseMapper.toEntity(request)).thenReturn(course);
        when(courseRepository.save(course)).thenReturn(saved);
        when(courseMapper.toResponse(saved)).thenReturn(response);

        CourseResponse result = courseService.create(request, 1L);

        assertThat(result.getIsPublished()).isFalse();
        assertThat(course.getInstructor()).isEqualTo(instructor);
        assertThat(course.getIsPublished()).isFalse();
    }

    @Test
    void create_missingInstructor_throwsNotFound() {
        CourseCreateRequest request = CourseCreateRequest.builder().title("Java").build();

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.create(request, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Instructor not found");
    }

    @Test
    void delete_existingCourse_removesRecord() {
        when(courseRepository.existsById(10L)).thenReturn(true);

        courseService.delete(10L);

        verify(courseRepository).deleteById(10L);
    }

    @Test
    void delete_missingCourse_throwsNotFound() {
        when(courseRepository.existsById(10L)).thenReturn(false);

        assertThatThrownBy(() -> courseService.delete(10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course not found");
    }

    @Test
    void findById_existingCourse_returnsResponse() {
        Course course = Course.builder().id(10L).title("Java").build();
        CourseResponse response = CourseResponse.builder().id(10L).title("Java").build();

        when(courseRepository.findByIdWithInstructor(10L)).thenReturn(Optional.of(course));
        when(courseMapper.toResponse(course)).thenReturn(response);

        CourseResponse result = courseService.findById(10L);

        assertThat(result.getTitle()).isEqualTo("Java");
    }
}
