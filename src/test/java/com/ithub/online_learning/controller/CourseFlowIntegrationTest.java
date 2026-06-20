package com.ithub.online_learning.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ithub.online_learning.dto.request.CourseCreateRequest;
import com.ithub.online_learning.dto.request.CourseUpdateRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CourseFlowIntegrationTest {

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

    @BeforeEach
    void setUp() {
        String suffix = IntegrationTestSupport.uniqueId();
        adminUsername = "admin_course_" + suffix;
        studentUsername = "student_course_" + suffix;

        IntegrationTestSupport.createUser(userRepository, roleRepository, passwordEncoder, adminUsername, "ROLE_ADMIN");
        IntegrationTestSupport.createUser(userRepository, roleRepository, passwordEncoder, studentUsername, "ROLE_STUDENT");
    }

    @Test
    void createPublishedCourse_studentSeesItInList() throws Exception {
        CourseCreateRequest createRequest = CourseCreateRequest.builder()
                .title("Visible Course")
                .description("Published for students")
                .isPublished(true)
                .build();

        String createResponse = mockMvc.perform(post("/courses")
                        .with(httpBasic(adminUsername, "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isPublished").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long courseId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/courses").with(httpBasic(studentUsername, "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == " + courseId + ")].title").value("Visible Course"));
    }

    @Test
    void updateAndDeleteCourse_adminCanManageCourse() throws Exception {
        CourseCreateRequest createRequest = CourseCreateRequest.builder()
                .title("Draft Course")
                .description("Will be updated")
                .isPublished(false)
                .build();

        String createResponse = mockMvc.perform(post("/courses")
                        .with(httpBasic(adminUsername, "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long courseId = objectMapper.readTree(createResponse).get("id").asLong();

        CourseUpdateRequest updateRequest = CourseUpdateRequest.builder()
                .title("Updated Course")
                .description("Updated description")
                .isPublished(true)
                .build();

        mockMvc.perform(put("/courses/{id}", courseId)
                        .with(httpBasic(adminUsername, "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Course"))
                .andExpect(jsonPath("$.isPublished").value(true));

        mockMvc.perform(get("/courses/{id}/detail", courseId)
                        .with(httpBasic(adminUsername, "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Course"))
                .andExpect(jsonPath("$.modules").isArray());

        mockMvc.perform(delete("/courses/{id}", courseId)
                        .with(httpBasic(adminUsername, "password")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/courses/{id}", courseId)
                        .with(httpBasic(adminUsername, "password")))
                .andExpect(status().isNotFound());
    }
}
