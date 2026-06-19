package com.ithub.online_learning.repository;

import com.ithub.online_learning.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @EntityGraph(attributePaths = "instructor")
    Page<Course> findByIsPublishedTrue(Pageable pageable);

    @EntityGraph(attributePaths = "instructor")
    @Query("SELECT c FROM Course c WHERE c.instructor.id = :instructorId")
    List<Course> findByInstructorId(@Param("instructorId") Long instructorId);

    @EntityGraph(attributePaths = "instructor")
    @Query("SELECT c FROM Course c WHERE c.id = :id")
    Optional<Course> findByIdWithInstructor(@Param("id") Long id);
}
