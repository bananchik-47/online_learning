package com.ithub.online_learning.repository;

import com.ithub.online_learning.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = "role")
    Optional<User> findByUsername(String username);

    @EntityGraph(attributePaths = "role")
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithRole(@Param("id") Long id);

    @EntityGraph(attributePaths = "role")
    @Query("SELECT u FROM User u")
    Page<User> findAllWithRole(Pageable pageable);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
