package com.ithub.online_learning.repository;

import com.ithub.online_learning.entity.CourseModule;
import com.ithub.online_learning.entity.Lesson;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql(statements = {
        "INSERT INTO roles (id, name) VALUES (1, 'ROLE_ADMIN')",
        "INSERT INTO users (id, username, email, password, enabled, role_id) VALUES (1, 'admin', 'admin@test.com', 'hash', true, 1)",
        "INSERT INTO courses (id, title, instructor_id, is_published) VALUES (1, 'Course', 1, true)",
        "INSERT INTO course_modules (id, course_id, title, order_index) VALUES (1, 1, 'Module A', 0)",
        "INSERT INTO course_modules (id, course_id, title, order_index) VALUES (2, 1, 'Module B', 1)"
})
class LessonRepositoryTest {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByModuleIdOrderByOrderIndexAsc_returnsLessonsInOrder() {
        CourseModule module = entityManager.find(CourseModule.class, 1L);

        entityManager.persist(Lesson.builder().module(module).title("Lesson 2").orderIndex(1).build());
        entityManager.persist(Lesson.builder().module(module).title("Lesson 1").orderIndex(0).build());
        entityManager.flush();

        List<Lesson> lessons = lessonRepository.findByModuleIdOrderByOrderIndexAsc(1L);

        assertThat(lessons).hasSize(2);
        assertThat(lessons).extracting(Lesson::getTitle).containsExactly("Lesson 1", "Lesson 2");
    }

    @Test
    void findById_returnsPersistedLesson() {
        CourseModule module = entityManager.find(CourseModule.class, 1L);
        Lesson lesson = Lesson.builder().module(module).title("Detailed lesson").orderIndex(0).build();
        entityManager.persist(lesson);
        entityManager.flush();
        entityManager.clear();

        Lesson found = lessonRepository.findById(lesson.getId()).orElseThrow();

        assertThat(found.getTitle()).isEqualTo("Detailed lesson");
        assertThat(found.getModule().getId()).isEqualTo(1L);
    }
}
