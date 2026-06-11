package com.ithub.online_learning.service.impl;

import com.ithub.online_learning.dto.request.CourseModuleCreateRequest;
import com.ithub.online_learning.dto.request.CourseModuleUpdateRequest;
import com.ithub.online_learning.dto.response.CourseModuleResponse;
import com.ithub.online_learning.entity.Course;
import com.ithub.online_learning.entity.CourseModule;
import com.ithub.online_learning.exception.ResourceNotFoundException;
import com.ithub.online_learning.mapper.CourseModuleMapper;
import com.ithub.online_learning.repository.CourseModuleRepository;
import com.ithub.online_learning.repository.CourseRepository;
import com.ithub.online_learning.service.CourseModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseModuleServiceImpl implements CourseModuleService {

    private final CourseModuleRepository courseModuleRepository;
    private final CourseRepository courseRepository;
    private final CourseModuleMapper courseModuleMapper;

    @Override
    public CourseModuleResponse create(Long courseId, CourseModuleCreateRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));

        CourseModule module = courseModuleMapper.toEntity(request);
        module.setCourse(course);

        return courseModuleMapper.toResponse(courseModuleRepository.save(module));
    }

    @Override
    public CourseModuleResponse update(Long id, CourseModuleUpdateRequest request) {
        CourseModule module = getModuleById(id);
        courseModuleMapper.updateEntity(request, module);
        return courseModuleMapper.toResponse(courseModuleRepository.save(module));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseModuleResponse findById(Long id) {
        CourseModule module = getModuleById(id);
        module.getLessons().size();
        return courseModuleMapper.toResponse(module);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseModuleResponse> findByCourseId(Long courseId) {
        List<CourseModule> modules = courseModuleRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        modules.forEach(module -> module.getLessons().size());
        return courseModuleMapper.toResponseList(modules);
    }

    @Override
    public void delete(Long id) {
        if (!courseModuleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course module not found: " + id);
        }
        courseModuleRepository.deleteById(id);
    }

    private CourseModule getModuleById(Long id) {
        return courseModuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course module not found: " + id));
    }
}
