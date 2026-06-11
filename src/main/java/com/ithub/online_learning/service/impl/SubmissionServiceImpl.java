package com.ithub.online_learning.service.impl;

import com.ithub.online_learning.dto.request.SubmissionGradeRequest;
import com.ithub.online_learning.dto.request.SubmissionRequest;
import com.ithub.online_learning.dto.response.SubmissionResponse;
import com.ithub.online_learning.entity.Assignment;
import com.ithub.online_learning.entity.Submission;
import com.ithub.online_learning.entity.User;
import com.ithub.online_learning.entity.enums.SubmissionStatus;
import com.ithub.online_learning.exception.BadRequestException;
import com.ithub.online_learning.exception.ResourceNotFoundException;
import com.ithub.online_learning.mapper.SubmissionMapper;
import com.ithub.online_learning.repository.AssignmentRepository;
import com.ithub.online_learning.repository.SubmissionRepository;
import com.ithub.online_learning.repository.UserRepository;
import com.ithub.online_learning.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final SubmissionMapper submissionMapper;

    @Override
    public SubmissionResponse submit(Long assignmentId, Long studentId, SubmissionRequest request) {
        if (submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId).isPresent()) {
            throw new BadRequestException("Submission already exists for this assignment");
        }

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + assignmentId));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));

        Submission submission = Submission.builder()
                .assignment(assignment)
                .student(student)
                .content(request.getContent())
                .status(SubmissionStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .build();

        return submissionMapper.toResponse(submissionRepository.save(submission));
    }

    @Override
    public SubmissionResponse update(Long assignmentId, Long studentId, SubmissionRequest request) {
        Submission submission = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Submission not found for assignment: " + assignmentId + " and student: " + studentId));

        submissionMapper.updateFromRequest(request, submission);
        submission.setStatus(SubmissionStatus.SUBMITTED);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setGradedAt(null);
        submission.setScore(null);
        submission.setFeedback(null);

        return submissionMapper.toResponse(submissionRepository.save(submission));
    }

    @Override
    public SubmissionResponse grade(Long id, SubmissionGradeRequest request) {
        Submission submission = getSubmissionById(id);

        if (request.getScore() > submission.getAssignment().getMaxScore()) {
            throw new BadRequestException("Score cannot exceed max score: " + submission.getAssignment().getMaxScore());
        }

        submissionMapper.updateGradeFromRequest(request, submission);
        submission.setGradedAt(LocalDateTime.now());

        return submissionMapper.toResponse(submissionRepository.save(submission));
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionResponse findById(Long id) {
        Submission submission = getSubmissionById(id);
        submission.getUploadedFiles().size();
        return submissionMapper.toResponse(submission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionResponse> findByAssignmentId(Long assignmentId) {
        return submissionMapper.toResponseList(submissionRepository.findByAssignmentId(assignmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionResponse> findByStudentId(Long studentId) {
        return submissionMapper.toResponseList(submissionRepository.findByStudentId(studentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionResponse> findPendingGrading() {
        return submissionMapper.toResponseList(submissionRepository.findByStatus(SubmissionStatus.SUBMITTED));
    }

    private Submission getSubmissionById(Long id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found: " + id));
    }
}
