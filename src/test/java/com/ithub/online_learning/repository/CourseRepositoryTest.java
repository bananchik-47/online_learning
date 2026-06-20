package com.ithub.online_learning.repository;

import com.ithub.online_learning.entity.Course;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql(statements = {
        "INSERT INTO roles (id, name) VALUES (1, 'ROLE_ADMIN')",
        "INSERT INTO users (id, username, email, password, enabled, role_id) VALUES (1, 'admin', 'admin@test.com', 'hash', true, 1)",
        "INSERT INTO courses (id, title, instructor_id, is_published) VALUES (1, 'Published Course', 1, true)",
        "INSERT INTO courses (id, title, instructor_id, is_published) VALUES (2, 'Draft Course', 1, false)"
})
class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    @Test
    void findByIsPublishedTrue_returnsOnlyPublishedCourses() {
        Page<Course> page = courseRepository.findByIsPublishedTrue(PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getTitle()).isEqualTo("Published Course");
        assertThat(page.getContent().getFirst().getInstructor().getUsername()).isEqualTo("admin");
    }

    @Test
    void findByInstructorId_returnsCoursesForInstructor() {
        List<Course> courses = courseRepository.findByInstructorId(1L);

        assertThat(courses).hasSize(2);
        assertThat(courses).extracting(Course::getTitle)
                .containsExactlyInAnyOrder("Published Course", "Draft Course");
    }

    @Test
    void findByIdWithInstructor_loadsInstructorRelationship() {
        Course course = courseRepository.findByIdWithInstructor(1L).orElseThrow();

        assertThat(course.getInstructor()).isNotNull();
        assertThat(course.getInstructor().getUsername()).isEqualTo("admin");
    }
}
