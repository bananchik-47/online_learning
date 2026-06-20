package com.ithub.online_learning.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ithub.online_learning.dto.request.CourseCreateRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EnrollmentFlowIntegrationTest {

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
    private Long publishedCourseId;
    private Long draftCourseId;

    @BeforeEach
    void setUp() throws Exception {
        String suffix = IntegrationTestSupport.uniqueId();
        adminUsername = "admin_enroll_" + suffix;
        studentUsername = "student_enroll_" + suffix;

        IntegrationTestSupport.createUser(userRepository, roleRepository, passwordEncoder, adminUsername, "ROLE_ADMIN");
        IntegrationTestSupport.createUser(userRepository, roleRepository, passwordEncoder, studentUsername, "ROLE_STUDENT");

        publishedCourseId = createCourse(true);
        draftCourseId = createCourse(false);
    }

    @Test
    void enroll_studentInPublishedCourse_returnsCreatedEnrollment() throws Exception {
        mockMvc.perform(post("/enrollments/courses/{courseId}", publishedCourseId)
                        .with(httpBasic(studentUsername, "password")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseId").value(publishedCourseId))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(get("/enrollments/me").with(httpBasic(studentUsername, "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].courseId").value(publishedCourseId));
    }

    @Test
    void enroll_duplicateEnrollment_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/enrollments/courses/{courseId}", publishedCourseId)
                        .with(httpBasic(studentUsername, "password")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/enrollments/courses/{courseId}", publishedCourseId)
                        .with(httpBasic(studentUsername, "password")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad request"));
    }

    @Test
    void enroll_unpublishedCourse_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/enrollments/courses/{courseId}", draftCourseId)
                        .with(httpBasic(studentUsername, "password")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Course is not published: " + draftCourseId));
    }

    @Test
    void enroll_adminUser_returnsForbidden() throws Exception {
        mockMvc.perform(post("/enrollments/courses/{courseId}", publishedCourseId)
                        .with(httpBasic(adminUsername, "password")))
                .andExpect(status().isForbidden());
    }

    private Long createCourse(boolean published) throws Exception {
        CourseCreateRequest request = CourseCreateRequest.builder()
                .title(published ? "Published" : "Draft")
                .description("Test course")
                .isPublished(published)
                .build();

        String response = mockMvc.perform(post("/courses")
                        .with(httpBasic(adminUsername, "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }
}
