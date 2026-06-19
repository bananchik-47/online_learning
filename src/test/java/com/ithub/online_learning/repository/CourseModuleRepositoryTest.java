package com.ithub.online_learning.repository;

import com.ithub.online_learning.entity.Course;
import com.ithub.online_learning.entity.CourseModule;
import com.ithub.online_learning.entity.Role;
import com.ithub.online_learning.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql(statements = {
        "INSERT INTO roles (id, name) VALUES (1, 'ROLE_ADMIN')",
        "INSERT INTO users (id, username, email, password, enabled, role_id) VALUES (1, 'admin', 'admin@test.com', 'hash', true, 1)",
        "INSERT INTO courses (id, title, instructor_id, is_published) VALUES (1, 'Java Basics', 1, true)"
})
class CourseModuleRepositoryTest {

    @Autowired
    private CourseModuleRepository courseModuleRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Test
    void findByCourseIdOrderByOrderIndexAsc_returnsSavedModules() {
        Course course = courseRepository.findById(1L).orElseThrow();

        CourseModule module = CourseModule.builder()
                .course(course)
                .title("Module 1")
                .orderIndex(0)
                .build();
        courseModuleRepository.saveAndFlush(module);

        List<CourseModule> found = courseModuleRepository.findByCourseIdOrderByOrderIndexAsc(1L);

        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getTitle()).isEqualTo("Module 1");
        assertThat(found.getFirst().getCourse().getId()).isEqualTo(1L);
    }
}
