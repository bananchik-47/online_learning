package com.ithub.online_learning.repository;

import com.ithub.online_learning.entity.Lesson;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    @Query("SELECT l FROM Lesson l WHERE l.module.id = :moduleId ORDER BY l.orderIndex ASC")
    List<Lesson> findByModuleIdOrderByOrderIndexAsc(@Param("moduleId") Long moduleId);

    @EntityGraph(attributePaths = "assignments")
    @Query("SELECT l FROM Lesson l WHERE l.id = :id")
    Optional<Lesson> findByIdWithAssignments(@Param("id") Long id);
}
