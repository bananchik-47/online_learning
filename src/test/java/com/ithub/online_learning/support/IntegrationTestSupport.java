package com.ithub.online_learning.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ithub.online_learning.dto.request.AssignmentCreateRequest;
import com.ithub.online_learning.dto.request.CourseCreateRequest;
import com.ithub.online_learning.dto.request.CourseModuleCreateRequest;
import com.ithub.online_learning.dto.request.LessonCreateRequest;
import com.ithub.online_learning.entity.Role;
import com.ithub.online_learning.entity.User;
import com.ithub.online_learning.repository.RoleRepository;
import com.ithub.online_learning.repository.UserRepository;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public final class IntegrationTestSupport {

    private IntegrationTestSupport() {
    }

    public record CourseHierarchyIds(Long courseId, Long moduleId, Long lessonId, Long assignmentId) {
    }

    public static String uniqueId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public static Role ensureRole(RoleRepository roleRepository, String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(Role.builder().name(roleName).build()));
    }

    public static User createUser(UserRepository userRepository,
                                  RoleRepository roleRepository,
                                  PasswordEncoder passwordEncoder,
                                  String username,
                                  String roleName) {
        Role role = ensureRole(roleRepository, roleName);
        return userRepository.save(User.builder()
                .username(username)
                .email(username + "@test.com")
                .password(passwordEncoder.encode("password"))
                .enabled(true)
                .role(role)
                .build());
    }

    public static CourseHierarchyIds createPublishedCourseHierarchy(MockMvc mockMvc,
                                                                  ObjectMapper objectMapper,
                                                                  String adminUsername) throws Exception {
        CourseCreateRequest courseRequest = CourseCreateRequest.builder()
                .title("Integration Course")
                .description("Course for integration tests")
                .isPublished(true)
                .build();

        Long courseId = extractId(mockMvc.perform(post("/courses")
                        .with(httpBasic(adminUsername, "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString());

        CourseModuleCreateRequest moduleRequest = CourseModuleCreateRequest.builder()
                .title("Module 1")
                .description("Intro")
                .orderIndex(0)
                .build();

        Long moduleId = extractId(mockMvc.perform(post("/courses/{courseId}/modules", courseId)
                        .with(httpBasic(adminUsername, "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moduleRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString());

        LessonCreateRequest lessonRequest = LessonCreateRequest.builder()
                .title("Lesson 1")
                .content("Lesson content")
                .orderIndex(0)
                .build();

        Long lessonId = extractId(mockMvc.perform(post("/modules/{moduleId}/lessons", moduleId)
                        .with(httpBasic(adminUsername, "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lessonRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString());

        AssignmentCreateRequest assignmentRequest = AssignmentCreateRequest.builder()
                .title("Homework 1")
                .description("Complete the task")
                .maxScore(100)
                .build();

        Long assignmentId = extractId(mockMvc.perform(post("/lessons/{lessonId}/assignments", lessonId)
                        .with(httpBasic(adminUsername, "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignmentRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString());

        return new CourseHierarchyIds(courseId, moduleId, lessonId, assignmentId);
    }

    private static Long extractId(String json) throws Exception {
        JsonNode node = new ObjectMapper().readTree(json);
        if (!node.has("id")) {
            throw new IllegalStateException("Response does not contain id: " + json);
        }
        return node.get("id").asLong();
    }
}
