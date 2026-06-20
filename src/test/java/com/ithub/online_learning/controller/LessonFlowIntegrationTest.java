package com.ithub.online_learning.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ithub.online_learning.dto.request.LessonCreateRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LessonFlowIntegrationTest {

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
    private Long moduleId;

    @BeforeEach
    void setUp() throws Exception {
        String suffix = IntegrationTestSupport.uniqueId();
        adminUsername = "admin_lesson_" + suffix;

        IntegrationTestSupport.createUser(userRepository, roleRepository, passwordEncoder, adminUsername, "ROLE_ADMIN");

        IntegrationTestSupport.CourseHierarchyIds hierarchy = IntegrationTestSupport.createPublishedCourseHierarchy(
                mockMvc, objectMapper, adminUsername);
        moduleId = hierarchy.moduleId();
    }

    @Test
    void createAndDeleteLesson_adminCanManageLessons() throws Exception {
        LessonCreateRequest createRequest = LessonCreateRequest.builder()
                .title("New Lesson")
                .content("Lesson body")
                .orderIndex(1)
                .build();

        String createResponse = mockMvc.perform(post("/modules/{moduleId}/lessons", moduleId)
                        .with(httpBasic(adminUsername, "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Lesson"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long lessonId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/modules/{moduleId}/lessons", moduleId)
                        .with(httpBasic(adminUsername, "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + lessonId + ")].title").value("New Lesson"));

        mockMvc.perform(delete("/lessons/{id}", lessonId)
                        .with(httpBasic(adminUsername, "password")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/modules/{moduleId}/lessons", moduleId)
                        .with(httpBasic(adminUsername, "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + lessonId + ")]").isEmpty());
    }
}
