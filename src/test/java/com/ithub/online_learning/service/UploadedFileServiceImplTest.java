package com.ithub.online_learning.service;

import com.ithub.online_learning.dto.response.UploadedFileResponse;
import com.ithub.online_learning.entity.Lesson;
import com.ithub.online_learning.entity.UploadedFile;
import com.ithub.online_learning.entity.User;
import com.ithub.online_learning.exception.BadRequestException;
import com.ithub.online_learning.mapper.UploadedFileMapper;
import com.ithub.online_learning.repository.LessonRepository;
import com.ithub.online_learning.repository.SubmissionRepository;
import com.ithub.online_learning.repository.UploadedFileRepository;
import com.ithub.online_learning.repository.UserRepository;
import com.ithub.online_learning.service.impl.UploadedFileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadedFileServiceImplTest {

    @TempDir
    Path tempDir;

    @Mock
    private UploadedFileRepository uploadedFileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private UploadedFileMapper uploadedFileMapper;

    @InjectMocks
    private UploadedFileServiceImpl uploadedFileService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(uploadedFileService, "uploadDir", tempDir.toString());
    }

    @Test
    void uploadForLesson_validFile_persistsMetadataAndContent() {
        User user = User.builder().id(1L).build();
        Lesson lesson = Lesson.builder().id(10L).build();
        UploadedFile saved = UploadedFile.builder()
                .id(100L)
                .originalFilename("notes.pdf")
                .storedFilename("stored-name")
                .fileSize(5L)
                .build();
        UploadedFileResponse response = UploadedFileResponse.builder()
                .id(100L)
                .originalFilename("notes.pdf")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));
        when(uploadedFileRepository.save(any(UploadedFile.class))).thenReturn(saved);
        when(uploadedFileMapper.toResponse(saved)).thenReturn(response);

        UploadedFileResponse result = uploadedFileService.uploadForLesson(
                "hello".getBytes(), "notes.pdf", "application/pdf", 1L, 10L);

        assertThat(result.getOriginalFilename()).isEqualTo("notes.pdf");
    }

    @Test
    void uploadForLesson_emptyContent_throwsBadRequest() {
        User user = User.builder().id(1L).build();
        Lesson lesson = Lesson.builder().id(10L).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));

        assertThatThrownBy(() -> uploadedFileService.uploadForLesson(
                new byte[0], "notes.pdf", "application/pdf", 1L, 10L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("File content is empty");
    }

    @Test
    void uploadForLesson_blankFilename_throwsBadRequest() {
        User user = User.builder().id(1L).build();
        Lesson lesson = Lesson.builder().id(10L).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));

        assertThatThrownBy(() -> uploadedFileService.uploadForLesson(
                "hello".getBytes(), "  ", "application/pdf", 1L, 10L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Original filename is required");
    }
}
