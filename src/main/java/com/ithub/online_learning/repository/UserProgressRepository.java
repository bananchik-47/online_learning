package com.ithub.online_learning.repository;

import com.ithub.online_learning.entity.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {

    Optional<UserProgress> findByUserIdAndLessonId(Long userId, Long lessonId);

    List<UserProgress> findByUserId(Long userId);
}
