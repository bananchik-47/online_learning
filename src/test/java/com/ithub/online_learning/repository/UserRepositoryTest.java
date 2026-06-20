package com.ithub.online_learning.repository;

import com.ithub.online_learning.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql(statements = {
        "INSERT INTO roles (id, name) VALUES (1, 'ROLE_ADMIN'), (2, 'ROLE_STUDENT')",
        "INSERT INTO users (id, username, email, password, enabled, role_id) VALUES (1, 'admin', 'admin@test.com', 'hash', true, 1)",
        "INSERT INTO users (id, username, email, password, enabled, role_id) VALUES (2, 'student', 'student@test.com', 'hash', true, 2)"
})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_loadsRoleRelationship() {
        User user = userRepository.findByUsername("student").orElseThrow();

        assertThat(user.getRole().getName()).isEqualTo("ROLE_STUDENT");
    }

    @Test
    void existsByUsernameAndEmail_detectDuplicates() {
        assertThat(userRepository.existsByUsername("admin")).isTrue();
        assertThat(userRepository.existsByUsername("missing")).isFalse();
        assertThat(userRepository.existsByEmail("student@test.com")).isTrue();
        assertThat(userRepository.existsByEmail("missing@test.com")).isFalse();
    }

    @Test
    void findAllWithRole_returnsUsersWithRoles() {
        Page<User> page = userRepository.findAllWithRole(PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent()).extracting(user -> user.getRole().getName())
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_STUDENT");
    }

    @Test
    void findByIdWithRole_loadsRoleForUser() {
        User user = userRepository.findByIdWithRole(1L).orElseThrow();

        assertThat(user.getUsername()).isEqualTo("admin");
        assertThat(user.getRole().getName()).isEqualTo("ROLE_ADMIN");
    }
}
