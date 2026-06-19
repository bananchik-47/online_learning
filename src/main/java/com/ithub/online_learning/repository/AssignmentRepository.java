package com.ithub.online_learning.repository;

import com.ithub.online_learning.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    @Query("SELECT a FROM Assignment a WHERE a.lesson.id = :lessonId")
    List<Assignment> findByLessonId(@Param("lessonId") Long lessonId);
}
