package com.ithub.online_learning.service;

import com.ithub.online_learning.dto.response.UploadedFileResponse;

import java.util.List;

public interface UploadedFileService {

    UploadedFileResponse uploadForSubmission(byte[] content, String originalFilename, String contentType,
                                             Long uploadedById, Long submissionId);

    UploadedFileResponse uploadForLesson(byte[] content, String originalFilename, String contentType,
                                         Long uploadedById, Long lessonId);

    UploadedFileResponse findById(Long id);

    List<UploadedFileResponse> findBySubmissionId(Long submissionId);

    List<UploadedFileResponse> findByLessonId(Long lessonId);

    byte[] getFileContent(Long id);

    void delete(Long id);
}
