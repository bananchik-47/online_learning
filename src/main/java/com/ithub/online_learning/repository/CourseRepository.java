package com.ithub.online_learning.repository;

import com.ithub.online_learning.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Page<Course> findByIsPublishedTrue(Pageable pageable);

    List<Course> findByInstructorId(Long instructorId);
}
