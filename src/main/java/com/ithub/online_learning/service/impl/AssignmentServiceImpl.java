package com.ithub.online_learning.service.impl;

import com.ithub.online_learning.dto.request.AssignmentCreateRequest;
import com.ithub.online_learning.dto.request.AssignmentUpdateRequest;
import com.ithub.online_learning.dto.response.AssignmentResponse;
import com.ithub.online_learning.entity.Assignment;
import com.ithub.online_learning.entity.Lesson;
import com.ithub.online_learning.exception.ResourceNotFoundException;
import com.ithub.online_learning.mapper.AssignmentMapper;
import com.ithub.online_learning.repository.AssignmentRepository;
import com.ithub.online_learning.repository.LessonRepository;
import com.ithub.online_learning.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final LessonRepository lessonRepository;
    private final AssignmentMapper assignmentMapper;

    @Override
    public AssignmentResponse create(Long lessonId, AssignmentCreateRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + lessonId));

        Assignment assignment = assignmentMapper.toEntity(request);
        assignment.setLesson(lesson);

        return assignmentMapper.toResponse(assignmentRepository.save(assignment));
    }

    @Override
    public AssignmentResponse update(Long id, AssignmentUpdateRequest request) {
        Assignment assignment = getAssignmentById(id);
        assignmentMapper.updateEntity(request, assignment);
        return assignmentMapper.toResponse(assignmentRepository.save(assignment));
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentResponse findById(Long id) {
        return assignmentMapper.toResponse(getAssignmentById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> findByLessonId(Long lessonId) {
        return assignmentMapper.toResponseList(assignmentRepository.findByLessonId(lessonId));
    }

    @Override
    public void delete(Long id) {
        if (!assignmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Assignment not found: " + id);
        }
        assignmentRepository.deleteById(id);
    }

    private Assignment getAssignmentById(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + id));
    }
}
