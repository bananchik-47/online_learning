package com.ithub.online_learning.service.impl;

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
import com.ithub.online_learning.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentMapper enrollmentMapper;

    @Override
    public EnrollmentResponse enroll(Long userId, Long courseId) {
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new BadRequestException("User is already enrolled in course: " + courseId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));

        if (!Boolean.TRUE.equals(course.getIsPublished())) {
            throw new BadRequestException("Course is not published: " + courseId);
        }

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .enrolledAt(LocalDateTime.now())
                .build();

        return enrollmentMapper.toResponse(enrollmentRepository.save(enrollment));
    }

    @Override
    public EnrollmentResponse complete(Long id) {
        Enrollment enrollment = getEnrollmentById(id);
        enrollment.setStatus(EnrollmentStatus.COMPLETED);
        enrollment.setCompletedAt(LocalDateTime.now());
        return enrollmentMapper.toResponse(enrollmentRepository.save(enrollment));
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentResponse findById(Long id) {
        return enrollmentMapper.toResponse(getEnrollmentById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> findByUserId(Long userId) {
        return enrollmentMapper.toResponseList(enrollmentRepository.findByUserId(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> findByCourseId(Long courseId) {
        return enrollmentMapper.toResponseList(enrollmentRepository.findByCourseId(courseId));
    }

    private Enrollment getEnrollmentById(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found: " + id));
    }
}
