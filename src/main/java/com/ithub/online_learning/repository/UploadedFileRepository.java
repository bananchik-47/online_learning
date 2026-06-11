package com.ithub.online_learning.repository;

import com.ithub.online_learning.entity.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {

    List<UploadedFile> findBySubmissionId(Long submissionId);

    List<UploadedFile> findByLessonId(Long lessonId);
}
