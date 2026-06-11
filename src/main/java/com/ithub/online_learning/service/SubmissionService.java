package com.ithub.online_learning.service;

import com.ithub.online_learning.dto.request.SubmissionGradeRequest;
import com.ithub.online_learning.dto.request.SubmissionRequest;
import com.ithub.online_learning.dto.response.SubmissionResponse;

import java.util.List;

public interface SubmissionService {

    SubmissionResponse submit(Long assignmentId, Long studentId, SubmissionRequest request);

    SubmissionResponse update(Long assignmentId, Long studentId, SubmissionRequest request);

    SubmissionResponse grade(Long id, SubmissionGradeRequest request);

    SubmissionResponse findById(Long id);

    List<SubmissionResponse> findByAssignmentId(Long assignmentId);

    List<SubmissionResponse> findByStudentId(Long studentId);

    List<SubmissionResponse> findPendingGrading();
}
