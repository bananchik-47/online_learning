package com.ithub.online_learning.repository;

import com.ithub.online_learning.entity.Course;
import com.ithub.online_learning.entity.Enrollment;
import com.ithub.online_learning.entity.User;
import com.ithub.online_learning.entity.enums.EnrollmentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql(statements = {
        "INSERT INTO roles (id, name) VALUES (1, 'ROLE_ADMIN'), (2, 'ROLE_STUDENT')",
        "INSERT INTO users (id, username, email, password, enabled, role_id) VALUES (1, 'admin', 'admin@test.com', 'hash', true, 1)",
        "INSERT INTO users (id, username, email, password, enabled, role_id) VALUES (2, 'student', 'student@test.com', 'hash', true, 2)",
        "INSERT INTO courses (id, title, instructor_id, is_published) VALUES (1, 'Published Course', 1, true)"
})
class EnrollmentRepositoryTest {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void existsByUserIdAndCourseId_returnsTrueForExistingEnrollment() {
        persistEnrollment(1L, 2L);

        assertThat(enrollmentRepository.existsByUserIdAndCourseId(2L, 1L)).isTrue();
        assertThat(enrollmentRepository.existsByUserIdAndCourseId(2L, 999L)).isFalse();
    }

    @Test
    void findByUserId_returnsEnrollmentsForUser() {
        persistEnrollment(1L, 2L);

        List<Enrollment> enrollments = enrollmentRepository.findByUserId(2L);

        assertThat(enrollments).hasSize(1);
        assertThat(enrollments.getFirst().getCourse().getTitle()).isEqualTo("Published Course");
    }

    @Test
    void findByCourseId_returnsEnrollmentsForCourse() {
        persistEnrollment(1L, 2L);

        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(1L);

        assertThat(enrollments).hasSize(1);
        assertThat(enrollments.getFirst().getUser().getUsername()).isEqualTo("student");
    }

    private void persistEnrollment(Long courseId, Long userId) {
        User user = entityManager.find(User.class, userId);
        Course course = entityManager.find(Course.class, courseId);

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .enrolledAt(LocalDateTime.now())
                .build();

        entityManager.persist(enrollment);
        entityManager.flush();
    }
}
