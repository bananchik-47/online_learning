package com.ithub.online_learning.repository;

import com.ithub.online_learning.entity.CourseModule;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseModuleRepository extends JpaRepository<CourseModule, Long> {

    @EntityGraph(attributePaths = "lessons")
    @Query("SELECT m FROM CourseModule m WHERE m.course.id = :courseId ORDER BY m.orderIndex ASC")
    List<CourseModule> findByCourseIdOrderByOrderIndexAsc(@Param("courseId") Long courseId);

    @EntityGraph(attributePaths = "lessons")
    @Query("SELECT m FROM CourseModule m WHERE m.id = :id")
    Optional<CourseModule> findByIdWithLessons(@Param("id") Long id);
}
