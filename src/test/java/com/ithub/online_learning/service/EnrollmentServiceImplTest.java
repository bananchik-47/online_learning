package com.ithub.online_learning.service;

import com.ithub.online_learning.dto.response.EnrollmentResponse;
import com.ithub.online_learning.entity.Course;
import com.ithub.online_learning.entity.Enrollment;
import com.ithub.online_learning.entity.User;
import com.ithub.online_learning.entity.enums.EnrollmentStatus;
import com.ithub.online_learning.exception.BadRequestException;
import com.ithub.online_learning.exception.ResourceNotFoundException;
import com.ithub.online_learning.mapper.EnrollmentMapper;
import com.ithub.online_learning.repository.CourseRepository;
import com.ithub.online_learning.repository.EnrollmentRepository;
import com.ithub.online_learning.repository.UserRepository;
import com.ithub.online_learning.service.impl.EnrollmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class EnrollmentServiceImplTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private EnrollmentMapper enrollmentMapper;

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;

    @Test
    void enroll_publishedCourse_createsActiveEnrollment() {
        User user = User.builder().id(1L).username("student1").build();
        Course course = Course.builder().id(10L).title("Java").isPublished(true).build();
        Enrollment saved = Enrollment.builder()
                .id(100L)
                .user(user)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .build();
        EnrollmentResponse response = EnrollmentResponse.builder()
                .id(100L)
                .userId(1L)
                .courseId(10L)
                .status(EnrollmentStatus.ACTIVE)
                .build();

        when(enrollmentRepository.existsByUserIdAndCourseId(1L, 10L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(saved);
        when(enrollmentMapper.toResponse(saved)).thenReturn(response);

        EnrollmentResponse result = enrollmentService.enroll(1L, 10L);

        assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(result.getCourseId()).isEqualTo(10L);

        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(captor.getValue().getEnrolledAt()).isNotNull();
    }

    @Test
    void enroll_duplicateEnrollment_throwsBadRequest() {
        when(enrollmentRepository.existsByUserIdAndCourseId(1L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> enrollmentService.enroll(1L, 10L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already enrolled");
    }

    @Test
    void enroll_unpublishedCourse_throwsBadRequest() {
        User user = User.builder().id(1L).build();
        Course course = Course.builder().id(10L).isPublished(false).build();

        when(enrollmentRepository.existsByUserIdAndCourseId(1L, 10L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> enrollmentService.enroll(1L, 10L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not published");
    }

    @Test
    void enroll_missingCourse_throwsNotFound() {
        User user = User.builder().id(1L).build();

        when(enrollmentRepository.existsByUserIdAndCourseId(1L, 10L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(courseRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.enroll(1L, 10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course not found");
    }

    @Test
    void complete_existingEnrollment_setsCompletedStatus() {
        Enrollment enrollment = Enrollment.builder()
                .id(100L)
                .status(EnrollmentStatus.ACTIVE)
                .build();
        Enrollment saved = Enrollment.builder()
                .id(100L)
                .status(EnrollmentStatus.COMPLETED)
                .build();
        EnrollmentResponse response = EnrollmentResponse.builder()
                .id(100L)
                .status(EnrollmentStatus.COMPLETED)
                .build();

        when(enrollmentRepository.findById(100L)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(enrollment)).thenReturn(saved);
        when(enrollmentMapper.toResponse(saved)).thenReturn(response);

        EnrollmentResponse result = enrollmentService.complete(100L);

        assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.COMPLETED);
        assertThat(enrollment.getCompletedAt()).isNotNull();
    }
}
