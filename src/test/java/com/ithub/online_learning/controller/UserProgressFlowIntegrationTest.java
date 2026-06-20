package com.ithub.online_learning.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ithub.online_learning.dto.request.UserProgressRequest;
import com.ithub.online_learning.repository.RoleRepository;
import com.ithub.online_learning.repository.UserRepository;
import com.ithub.online_learning.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserProgressFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminUsername;
    private String studentUsername;
    private Long lessonId;

    @BeforeEach
    void setUp() throws Exception {
        String suffix = IntegrationTestSupport.uniqueId();
        adminUsername = "admin_progress_" + suffix;
        studentUsername = "student_progress_" + suffix;

        IntegrationTestSupport.createUser(userRepository, roleRepository, passwordEncoder, adminUsername, "ROLE_ADMIN");
        IntegrationTestSupport.createUser(userRepository, roleRepository, passwordEncoder, studentUsername, "ROLE_STUDENT");

        IntegrationTestSupport.CourseHierarchyIds hierarchy = IntegrationTestSupport.createPublishedCourseHierarchy(
                mockMvc, objectMapper, adminUsername);
        lessonId = hierarchy.lessonId();
    }

    @Test
    void updateProgress_marksLessonCompleted() throws Exception {
        UserProgressRequest request = UserProgressRequest.builder().completed(true).build();

        mockMvc.perform(put("/progress/lessons/{lessonId}", lessonId)
                        .with(httpBasic(studentUsername, "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lessonId").value(lessonId))
                .andExpect(jsonPath("$.completed").value(true))
                .andExpect(jsonPath("$.completedAt").exists());

        mockMvc.perform(get("/progress/lessons/{lessonId}", lessonId)
                        .with(httpBasic(studentUsername, "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));

        mockMvc.perform(get("/progress/me").with(httpBasic(studentUsername, "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void updateProgress_adminUser_returnsForbidden() throws Exception {
        UserProgressRequest request = UserProgressRequest.builder().completed(true).build();

        mockMvc.perform(put("/progress/lessons/{lessonId}", lessonId)
                        .with(httpBasic(adminUsername, "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
