package com.ithub.online_learning.service.impl;

import com.ithub.online_learning.dto.response.UploadedFileResponse;
import com.ithub.online_learning.entity.Lesson;
import com.ithub.online_learning.entity.Submission;
import com.ithub.online_learning.entity.UploadedFile;
import com.ithub.online_learning.entity.User;
import com.ithub.online_learning.exception.BadRequestException;
import com.ithub.online_learning.exception.ResourceNotFoundException;
import com.ithub.online_learning.mapper.UploadedFileMapper;
import com.ithub.online_learning.repository.LessonRepository;
import com.ithub.online_learning.repository.SubmissionRepository;
import com.ithub.online_learning.repository.UploadedFileRepository;
import com.ithub.online_learning.repository.UserRepository;
import com.ithub.online_learning.service.UploadedFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UploadedFileServiceImpl implements UploadedFileService {

    private final UploadedFileRepository uploadedFileRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final LessonRepository lessonRepository;
    private final UploadedFileMapper uploadedFileMapper;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public UploadedFileResponse uploadForSubmission(byte[] content, String originalFilename, String contentType,
                                                    Long uploadedById, Long submissionId) {
        User uploadedBy = getUser(uploadedById);
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found: " + submissionId));

        return saveFile(content, originalFilename, contentType, uploadedBy, submission, null);
    }

    @Override
    public UploadedFileResponse uploadForLesson(byte[] content, String originalFilename, String contentType,
                                                Long uploadedById, Long lessonId) {
        User uploadedBy = getUser(uploadedById);
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + lessonId));

        return saveFile(content, originalFilename, contentType, uploadedBy, null, lesson);
    }

    @Override
    @Transactional(readOnly = true)
    public UploadedFileResponse findById(Long id) {
        return uploadedFileMapper.toResponse(getUploadedFileById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UploadedFileResponse> findBySubmissionId(Long submissionId) {
        return uploadedFileMapper.toResponseList(uploadedFileRepository.findBySubmissionId(submissionId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UploadedFileResponse> findByLessonId(Long lessonId) {
        return uploadedFileMapper.toResponseList(uploadedFileRepository.findByLessonId(lessonId));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getFileContent(Long id) {
        UploadedFile uploadedFile = getUploadedFileById(id);
        try {
            return Files.readAllBytes(resolveStoragePath(uploadedFile.getStoredFilename()));
        } catch (IOException e) {
            throw new BadRequestException("Failed to read file: " + uploadedFile.getOriginalFilename());
        }
    }

    @Override
    public void delete(Long id) {
        UploadedFile uploadedFile = getUploadedFileById(id);
        try {
            Files.deleteIfExists(resolveStoragePath(uploadedFile.getStoredFilename()));
        } catch (IOException e) {
            throw new BadRequestException("Failed to delete file: " + uploadedFile.getOriginalFilename());
        }
        uploadedFileRepository.delete(uploadedFile);
    }

    private UploadedFileResponse saveFile(byte[] content, String originalFilename, String contentType,
                                          User uploadedBy, Submission submission, Lesson lesson) {
        if (content == null || content.length == 0) {
            throw new BadRequestException("File content is empty");
        }
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BadRequestException("Original filename is required");
        }

        String storedFilename = UUID.randomUUID().toString();
        Path storagePath = resolveStoragePath(storedFilename);

        try {
            Files.createDirectories(storagePath.getParent());
            Files.write(storagePath, content);
        } catch (IOException e) {
            throw new BadRequestException("Failed to store file: " + originalFilename);
        }

        UploadedFile uploadedFile = UploadedFile.builder()
                .originalFilename(originalFilename)
                .storedFilename(storedFilename)
                .contentType(contentType)
                .fileSize((long) content.length)
                .uploadedBy(uploadedBy)
                .submission(submission)
                .lesson(lesson)
                .build();

        return uploadedFileMapper.toResponse(uploadedFileRepository.save(uploadedFile));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private UploadedFile getUploadedFileById(Long id) {
        return uploadedFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Uploaded file not found: " + id));
    }

    private Path resolveStoragePath(String storedFilename) {
        return Paths.get(uploadDir).resolve(storedFilename);
    }
}
