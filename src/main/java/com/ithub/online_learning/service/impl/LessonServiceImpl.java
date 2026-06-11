package com.ithub.online_learning.service.impl;

import com.ithub.online_learning.dto.request.LessonCreateRequest;
import com.ithub.online_learning.dto.request.LessonUpdateRequest;
import com.ithub.online_learning.dto.response.LessonResponse;
import com.ithub.online_learning.dto.response.LessonSummaryResponse;
import com.ithub.online_learning.entity.CourseModule;
import com.ithub.online_learning.entity.Lesson;
import com.ithub.online_learning.exception.ResourceNotFoundException;
import com.ithub.online_learning.mapper.LessonMapper;
import com.ithub.online_learning.repository.CourseModuleRepository;
import com.ithub.online_learning.repository.LessonRepository;
import com.ithub.online_learning.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final CourseModuleRepository courseModuleRepository;
    private final LessonMapper lessonMapper;

    @Override
    public LessonResponse create(Long moduleId, LessonCreateRequest request) {
        CourseModule module = courseModuleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Course module not found: " + moduleId));

        Lesson lesson = lessonMapper.toEntity(request);
        lesson.setModule(module);

        return lessonMapper.toResponse(lessonRepository.save(lesson));
    }

    @Override
    public LessonResponse update(Long id, LessonUpdateRequest request) {
        Lesson lesson = getLessonById(id);
        lessonMapper.updateEntity(request, lesson);
        return lessonMapper.toResponse(lessonRepository.save(lesson));
    }

    @Override
    @Transactional(readOnly = true)
    public LessonResponse findById(Long id) {
        Lesson lesson = getLessonById(id);
        lesson.getAssignments().size();
        lesson.getUploadedFiles().size();
        return lessonMapper.toResponse(lesson);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonSummaryResponse> findByModuleId(Long moduleId) {
        return lessonMapper.toSummaryResponseList(
                lessonRepository.findByModuleIdOrderByOrderIndexAsc(moduleId)
        );
    }

    @Override
    public void delete(Long id) {
        if (!lessonRepository.existsById(id)) {
            throw new ResourceNotFoundException("Lesson not found: " + id);
        }
        lessonRepository.deleteById(id);
    }

    private Lesson getLessonById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + id));
    }
}
