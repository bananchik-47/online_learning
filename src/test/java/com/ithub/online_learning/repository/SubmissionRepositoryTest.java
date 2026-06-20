package com.ithub.online_learning.repository;

import com.ithub.online_learning.entity.Assignment;
import com.ithub.online_learning.entity.Lesson;
import com.ithub.online_learning.entity.Submission;
import com.ithub.online_learning.entity.User;
import com.ithub.online_learning.entity.enums.SubmissionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql(statements = {
        "INSERT INTO roles (id, name) VALUES (1, 'ROLE_ADMIN'), (2, 'ROLE_STUDENT')",
        "INSERT INTO users (id, username, email, password, enabled, role_id) VALUES (1, 'admin', 'admin@test.com', 'hash', true, 1)",
        "INSERT INTO users (id, username, email, password, enabled, role_id) VALUES (2, 'student', 'student@test.com', 'hash', true, 2)",
        "INSERT INTO courses (id, title, instructor_id, is_published) VALUES (1, 'Course', 1, true)",
        "INSERT INTO course_modules (id, course_id, title, order_index) VALUES (1, 1, 'Module', 0)",
        "INSERT INTO lessons (id, module_id, title, order_index) VALUES (1, 1, 'Lesson', 0)",
        "INSERT INTO assignments (id, lesson_id, title, max_score) VALUES (1, 1, 'HW', 100)"
})
class SubmissionRepositoryTest {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Submission submission;

    @BeforeEach
    void setUp() {
        User student = entityManager.find(User.class, 2L);
        Assignment assignment = entityManager.find(Assignment.class, 1L);

        submission = Submission.builder()
                .assignment(assignment)
                .student(student)
                .content("Answer")
                .status(SubmissionStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .build();

        entityManager.persist(submission);
        entityManager.flush();
    }

    @Test
    void findByAssignmentIdAndStudentId_returnsExistingSubmission() {
        Optional<Submission> found = submissionRepository.findByAssignmentIdAndStudentId(1L, 2L);

        assertThat(found).isPresent();
        assertThat(found.get().getContent()).isEqualTo("Answer");
    }

    @Test
    void findByStatus_returnsMatchingSubmissions() {
        List<Submission> pending = submissionRepository.findByStatus(SubmissionStatus.SUBMITTED);

        assertThat(pending).hasSize(1);
        assertThat(pending.getFirst().getStudent().getUsername()).isEqualTo("student");
    }

    @Test
    void findByAssignmentId_returnsSubmissionsForAssignment() {
        List<Submission> submissions = submissionRepository.findByAssignmentId(1L);

        assertThat(submissions).hasSize(1);
        assertThat(submissions.getFirst().getAssignment().getTitle()).isEqualTo("HW");
    }

    @Test
    void findByStudentId_returnsSubmissionsForStudent() {
        List<Submission> submissions = submissionRepository.findByStudentId(2L);

        assertThat(submissions).hasSize(1);
        assertThat(submissions.getFirst().getAssignment().getId()).isEqualTo(1L);
    }
}
