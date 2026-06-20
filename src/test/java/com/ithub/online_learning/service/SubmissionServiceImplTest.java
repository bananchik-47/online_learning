package com.ithub.online_learning.service;

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
import com.ithub.online_learning.service.impl.SubmissionServiceImpl;
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
class SubmissionServiceImplTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubmissionMapper submissionMapper;

    @InjectMocks
    private SubmissionServiceImpl submissionService;

    @Test
    void submit_newSubmission_createsSubmittedRecord() {
        Assignment assignment = Assignment.builder().id(5L).title("HW1").maxScore(100).build();
        User student = User.builder().id(2L).username("student1").build();
        Submission saved = Submission.builder()
                .id(50L)
                .assignment(assignment)
                .student(student)
                .content("Answer")
                .status(SubmissionStatus.SUBMITTED)
                .build();
        SubmissionResponse response = SubmissionResponse.builder()
                .id(50L)
                .content("Answer")
                .status(SubmissionStatus.SUBMITTED)
                .build();

        when(submissionRepository.findByAssignmentIdAndStudentId(5L, 2L)).thenReturn(Optional.empty());
        when(assignmentRepository.findById(5L)).thenReturn(Optional.of(assignment));
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(submissionRepository.save(any(Submission.class))).thenReturn(saved);
        when(submissionMapper.toResponse(saved)).thenReturn(response);

        SubmissionResponse result = submissionService.submit(5L, 2L, SubmissionRequest.builder().content("Answer").build());

        assertThat(result.getStatus()).isEqualTo(SubmissionStatus.SUBMITTED);
        assertThat(result.getContent()).isEqualTo("Answer");
    }

    @Test
    void submit_duplicateSubmission_throwsBadRequest() {
        when(submissionRepository.findByAssignmentIdAndStudentId(5L, 2L))
                .thenReturn(Optional.of(Submission.builder().id(50L).build()));

        assertThatThrownBy(() -> submissionService.submit(
                5L, 2L, SubmissionRequest.builder().content("Answer").build()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void update_existingSubmission_resetsGradeFields() {
        Assignment assignment = Assignment.builder().id(5L).maxScore(100).build();
        Submission submission = Submission.builder()
                .id(50L)
                .assignment(assignment)
                .content("Old answer")
                .score(90)
                .feedback("Good")
                .status(SubmissionStatus.GRADED)
                .build();
        SubmissionResponse response = SubmissionResponse.builder()
                .id(50L)
                .content("New answer")
                .status(SubmissionStatus.SUBMITTED)
                .build();

        when(submissionRepository.findByAssignmentIdAndStudentId(5L, 2L)).thenReturn(Optional.of(submission));
        when(submissionRepository.save(submission)).thenReturn(submission);
        when(submissionMapper.toResponse(submission)).thenReturn(response);

        SubmissionResponse result = submissionService.update(
                5L, 2L, SubmissionRequest.builder().content("New answer").build());

        assertThat(result.getStatus()).isEqualTo(SubmissionStatus.SUBMITTED);
        assertThat(submission.getScore()).isNull();
        assertThat(submission.getFeedback()).isNull();
        assertThat(submission.getGradedAt()).isNull();
        verify(submissionMapper).updateFromRequest(any(SubmissionRequest.class), any(Submission.class));
    }

    @Test
    void grade_validScore_updatesSubmission() {
        Assignment assignment = Assignment.builder().id(5L).maxScore(100).build();
        Submission submission = Submission.builder()
                .id(50L)
                .assignment(assignment)
                .status(SubmissionStatus.SUBMITTED)
                .build();
        SubmissionResponse response = SubmissionResponse.builder()
                .id(50L)
                .score(85)
                .status(SubmissionStatus.GRADED)
                .build();

        when(submissionRepository.findById(50L)).thenReturn(Optional.of(submission));
        when(submissionRepository.save(submission)).thenReturn(submission);
        when(submissionMapper.toResponse(submission)).thenReturn(response);

        SubmissionResponse result = submissionService.grade(
                50L, SubmissionGradeRequest.builder().score(85).feedback("Well done").build());

        assertThat(result.getScore()).isEqualTo(85);
        assertThat(submission.getGradedAt()).isNotNull();
        verify(submissionMapper).updateGradeFromRequest(any(SubmissionGradeRequest.class), any(Submission.class));
    }

    @Test
    void grade_scoreAboveMax_throwsBadRequest() {
        Assignment assignment = Assignment.builder().id(5L).maxScore(100).build();
        Submission submission = Submission.builder().id(50L).assignment(assignment).build();

        when(submissionRepository.findById(50L)).thenReturn(Optional.of(submission));

        assertThatThrownBy(() -> submissionService.grade(
                50L, SubmissionGradeRequest.builder().score(101).build()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot exceed max score");
    }
}
