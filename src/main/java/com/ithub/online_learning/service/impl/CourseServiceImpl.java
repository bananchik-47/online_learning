package com.ithub.online_learning.service.impl;

import com.ithub.online_learning.dto.request.CourseCreateRequest;
import com.ithub.online_learning.dto.request.CourseUpdateRequest;
import com.ithub.online_learning.dto.response.CourseDetailResponse;
import com.ithub.online_learning.dto.response.CourseResponse;
import com.ithub.online_learning.entity.Course;
import com.ithub.online_learning.entity.User;
import com.ithub.online_learning.exception.ResourceNotFoundException;
import com.ithub.online_learning.mapper.CourseMapper;
import com.ithub.online_learning.repository.CourseRepository;
import com.ithub.online_learning.repository.UserRepository;
import com.ithub.online_learning.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseMapper courseMapper;

    @Override
    public CourseResponse create(CourseCreateRequest request, Long instructorId) {
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found: " + instructorId));

        Course course = courseMapper.toEntity(request);
        course.setInstructor(instructor);
        if (course.getIsPublished() == null) {
            course.setIsPublished(false);
        }

        return courseMapper.toResponse(courseRepository.save(course));
    }

    @Override
    public CourseResponse update(Long id, CourseUpdateRequest request) {
        Course course = getCourseById(id);
        courseMapper.updateEntity(request, course);
        return courseMapper.toResponse(courseRepository.save(course));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse findById(Long id) {
        return courseMapper.toResponse(getCourseById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDetailResponse findDetailById(Long id) {
        Course course = getCourseById(id);
        course.getModules().forEach(module -> module.getLessons().size());
        return courseMapper.toDetailResponse(course);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponse> findPublished(Pageable pageable) {
        return courseRepository.findByIsPublishedTrue(pageable)
                .map(courseMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> findByInstructor(Long instructorId) {
        return courseMapper.toResponseList(courseRepository.findByInstructorId(instructorId));
    }

    @Override
    public void delete(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course not found: " + id);
        }
        courseRepository.deleteById(id);
    }

    private Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + id));
    }
}
